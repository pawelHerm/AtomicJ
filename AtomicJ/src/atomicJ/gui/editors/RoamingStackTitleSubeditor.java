package atomicJ.gui.editors;

import static atomicJ.gui.PreferenceKeys.*;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import atomicJ.data.units.PrefixedUnit;
import atomicJ.gui.CustomizableXYBaseChart;
import atomicJ.gui.PreferredAxisStyle;
import atomicJ.gui.RoamingStandardStackTextTitle;
import atomicJ.gui.RoamingTextTitle;
import atomicJ.gui.SubPanel;

import com.lowagie.text.Font;


public class RoamingStackTitleSubeditor extends RoamingTitleSubeditor 
{
    //CURRENT PARAMETERS

    private static final long serialVersionUID = 1L;

    private boolean updateFrameTitle;

    //format parameters
    private PrefixedUnit axisUnit;
    private int maxFractionDigits;
    private boolean groupingUsed;
    private char groupingSeparator;
    private char decimalSeparator;
    private boolean trailingZeroes;

    //INITIAL PARAMETERS

    private final boolean initUpdateFrameTitle;

    //initial format parameters
    private final PrefixedUnit initAxisUnit;
    private final int initMaxFractionDigits;
    private final boolean initGroupingUsed;
    private final char initroupingSeparator;
    private final char initDecimalSeparator;
    private final boolean initTrailingZeroes;

    private final JComboBox<PrefixedUnit> comboAxisUnits = new JComboBox<>();

    private final JCheckBox boxUpdateFrameTitle = new JCheckBox();

    private final JSpinner spinnerFractionDigits = new JSpinner(new SpinnerNumberModel(1,0,1000,1));

    private final JComboBox<Character> comboDecimalSeparator = new JComboBox<>(new Character[] {'.',','});
    private final JComboBox<Character> comboGroupingSeparator = new JComboBox<>(new Character[] {' ',',','.','\''});
    private final JCheckBox boxTrailingZeroes = new JCheckBox();
    private final JCheckBox boxUseThousandGrouping = new JCheckBox("Use separator");

    public RoamingStackTitleSubeditor(List<? extends CustomizableXYBaseChart<?>> boundedCharts, 
            CustomizableXYBaseChart<?> chart) 
    {
        super(boundedCharts, chart);

        RoamingStandardStackTextTitle titleWorking = (RoamingStandardStackTextTitle)getTitleWorking();

        this.initUpdateFrameTitle = titleWorking.isUpdateFrameTitle();

        this.initAxisUnit = titleWorking.getDisplayedUnit();
        this.initMaxFractionDigits = titleWorking.getMaximumFractionDigits();
        this.initGroupingUsed = titleWorking.isTickLabelGroupingUsed();
        this.initroupingSeparator = titleWorking.getTickLabelGroupingSeparator();
        this.initDecimalSeparator = titleWorking.getTickLabelDecimalSeparator();
        this.initTrailingZeroes = titleWorking.isTickLabelTrailingZeroes();

        setParametersToInitial();

        List<PrefixedUnit> proposedAxisUnits = titleWorking.getProposedUnits();

        for(PrefixedUnit unit : proposedAxisUnits)
        {
            comboAxisUnits.addItem(unit);
        }

        comboAxisUnits.setSelectedItem(initAxisUnit);
        boxUpdateFrameTitle.setSelected(initUpdateFrameTitle);

        spinnerFractionDigits.setValue(initMaxFractionDigits);

        comboDecimalSeparator.setSelectedItem(initDecimalSeparator);
        comboGroupingSeparator.setSelectedItem(initroupingSeparator);
        comboGroupingSeparator.setEnabled(initGroupingUsed);

        boxUseThousandGrouping.setSelected(initGroupingUsed);
        boxTrailingZeroes.setSelected(initTrailingZeroes);

        initChangeListener();
        initItemListener();

        JPanel panelFrameTitle = buildFrameTitlePanel();
        insertEditorTab(panelFrameTitle, "Updating", "", 1);
    }

    private void initChangeListener()
    {
        spinnerFractionDigits.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e)
            {
                RoamingStandardStackTextTitle stackTitle = (RoamingStandardStackTextTitle)getTitleWorking();

                maxFractionDigits = ((SpinnerNumberModel)spinnerFractionDigits.getModel()).getNumber().intValue();;
                stackTitle.setMaximumFractionDigits(maxFractionDigits);                
            }
        });
    }

    @Override
    protected void setParametersToInitial()
    {
        super.setParametersToInitial();

        this.updateFrameTitle = initUpdateFrameTitle;

        this.axisUnit = initAxisUnit;
        this.maxFractionDigits = initMaxFractionDigits;
        this.groupingUsed = initGroupingUsed ;
        this.groupingSeparator = initroupingSeparator;
        this.decimalSeparator = initDecimalSeparator;
        this.trailingZeroes = initTrailingZeroes;
    }

    private JPanel buildFrameTitlePanel()
    {
        JPanel formatPanel = new JPanel();
        SubPanel innerPanel = new SubPanel();   

        JLabel labelUpdateFrameTitle = new JLabel("Update frame: ");

        JLabel labelTrailingZeroes = new JLabel("Trailing zeroes: ");
        JLabel labelMaxFractionDigits = new JLabel("Fraction digits: ");
        JLabel labelDecimalSeparator = new JLabel("Decimal separator: ");
        JLabel labelThousandSeparator = new JLabel("Thousand separator: "); 
        JLabel labelDisplayedUnit = new JLabel("Unit: ");

        JLabel labelNumberFormat = new JLabel("Number format");
        labelNumberFormat.setFont(labelNumberFormat.getFont().deriveFont(Font.BOLD));

        comboDecimalSeparator.setPreferredSize(comboGroupingSeparator.getPreferredSize());

        innerPanel.addComponent(labelUpdateFrameTitle, 0, 0, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1, new Insets(10,6,6,6));
        innerPanel.addComponent(boxUpdateFrameTitle, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1, new Insets(10,6,6,6));

        innerPanel.addComponent(labelNumberFormat, 0, 1, 2, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1, new Insets(10,6,6,6));

        innerPanel.addComponent(labelTrailingZeroes, 0, 2, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);
        innerPanel.addComponent(boxTrailingZeroes, 1, 2, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);

        innerPanel.addComponent(labelMaxFractionDigits, 0, 3, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);
        innerPanel.addComponent(spinnerFractionDigits, 1, 3, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);

        innerPanel.addComponent(labelDecimalSeparator, 0, 4, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);
        innerPanel.addComponent(comboDecimalSeparator, 1, 4, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);

        innerPanel.addComponent(labelThousandSeparator, 0, 5, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);
        innerPanel.addComponent(comboGroupingSeparator, 1, 5, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);
        innerPanel.addComponent(boxUseThousandGrouping,2, 5, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 1);

        innerPanel.addComponent(labelDisplayedUnit, 0, 6, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1, 1);
        innerPanel.addComponent(comboAxisUnits, 1, 6, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, 1);

        formatPanel.add(innerPanel);
        formatPanel.setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 4));

        return formatPanel; 
    }

    @Override
    public void resetToDefaults() 
    {
        super.resetToDefaults();

        Preferences pref = getPreferences();

        this.maxFractionDigits = pref.getInt(TITLE_STACK_MAX_FRACTION_DIGITS, 3);
        this.groupingUsed = pref.getBoolean(PreferredAxisStyle.TICK_LABEL_GROUPING_USED, false);       
        this.trailingZeroes = pref.getBoolean(PreferredAxisStyle.TICK_LABEL_TRAILING_ZEROES, true);
        this.groupingSeparator = (char) pref.getInt(PreferredAxisStyle.TICK_LABEL_GROUPING_SEPARATOR, ' ');                
        this.decimalSeparator = (char) pref.getInt(PreferredAxisStyle.TICK_LABEL_DECIMAL_SEPARATOR, '.');              

        RoamingTextTitle titleWorking = getTitleWorking();
        resetTitle(titleWorking);
        resetEditor();
    }

    @Override
    public void saveAsDefaults() 
    { 
        super.saveAsDefaults();

        Preferences pref = getPreferences();

        pref.putInt(TITLE_STACK_MAX_FRACTION_DIGITS, maxFractionDigits);
        pref.putBoolean(PreferredAxisStyle.TICK_LABEL_GROUPING_USED, groupingUsed);       
        pref.putInt(PreferredAxisStyle.TICK_LABEL_GROUPING_SEPARATOR, groupingSeparator);             
        pref.putInt(PreferredAxisStyle.TICK_LABEL_DECIMAL_SEPARATOR, decimalSeparator);               
        pref.putBoolean(PreferredAxisStyle.TICK_LABEL_TRAILING_ZEROES, trailingZeroes);

        try 
        {
            pref.flush();
        } 
        catch (BackingStoreException e) 
        {
            e.printStackTrace();
        }
    }

    @Override
    protected void resetTitle(RoamingTextTitle t)
    {   
        super.resetTitle(t);

        if(t instanceof RoamingStandardStackTextTitle)
        {
            RoamingStandardStackTextTitle stackTitle = (RoamingStandardStackTextTitle)t;

            stackTitle.setUpdateFrameTitle(updateFrameTitle);
            stackTitle.setDisplayedUnit(axisUnit);

            stackTitle.setTickLabelGroupingUsed(groupingUsed);
            stackTitle.setTickLabelDecimalSeparator(decimalSeparator);
            stackTitle.setTickLabelGroupingSeparator(groupingSeparator);
            stackTitle.setTickLabelShowTrailingZeroes(trailingZeroes);
        }
    }

    @Override
    protected void resetEditor()
    {
        super.resetEditor();

        boxUpdateFrameTitle.setSelected(updateFrameTitle);

        comboAxisUnits.setSelectedItem(axisUnit);
        spinnerFractionDigits.setValue(maxFractionDigits);
        boxUseThousandGrouping.setSelected(groupingUsed);  
        boxTrailingZeroes.setSelected(trailingZeroes);
        comboDecimalSeparator.setSelectedItem(decimalSeparator);
        comboGroupingSeparator.setSelectedItem(groupingSeparator);   
    }



    private void initItemListener()
    {
        boxUpdateFrameTitle.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                updateFrameTitle = (e.getStateChange()== ItemEvent.SELECTED);
                ((RoamingStandardStackTextTitle)getTitleWorking()).setUpdateFrameTitle(updateFrameTitle);                
            }
        });

        comboAxisUnits.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                axisUnit = (PrefixedUnit)comboAxisUnits.getSelectedItem();
                ((RoamingStandardStackTextTitle)getTitleWorking()).setDisplayedUnit(axisUnit);                
            }
        });
        comboDecimalSeparator.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                decimalSeparator = (Character)comboDecimalSeparator.getSelectedItem();     
                ((RoamingStandardStackTextTitle)getTitleWorking()).setTickLabelDecimalSeparator(decimalSeparator);                    

            }
        });
        comboGroupingSeparator.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e)
            {
                groupingSeparator = (Character)comboGroupingSeparator.getSelectedItem();       
                ((RoamingStandardStackTextTitle)getTitleWorking()).setTickLabelGroupingSeparator(groupingSeparator);
            }
        });
        boxUseThousandGrouping.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                groupingUsed = (e.getStateChange()== ItemEvent.SELECTED);

                ((RoamingStandardStackTextTitle)getTitleWorking()).setTickLabelGroupingUsed(groupingUsed);
                comboGroupingSeparator.setEnabled(groupingUsed);                    
            }
        });
        boxTrailingZeroes.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                trailingZeroes = (e.getStateChange()== ItemEvent.SELECTED);
                ((RoamingStandardStackTextTitle)getTitleWorking()).setTickLabelShowTrailingZeroes(trailingZeroes);                
            }
        }); 
    }
}
