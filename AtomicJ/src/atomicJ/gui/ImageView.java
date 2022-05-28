
/* 
 * ===========================================================
 * AtomicJ : a free application for analysis of AFM data
 * ===========================================================
 *
 * (C) Copyright 2013-2022 by Pawe³ Hermanowicz
 *
 * 
 * This program is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program; if not, 
 * see http://www.gnu.org/licenses/*/

package atomicJ.gui;


import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import atomicJ.gui.Channel2DPanel.DensityPanelFactory;
import atomicJ.readers.ImageReadingModel;
import atomicJ.resources.Channel2DResource;
import atomicJ.resources.StandardChannel2DResource;
import atomicJ.sources.ImageSource;
import atomicJ.sources.ChannelSource;

public class ImageView extends Channel2DView<StandardChannel2DResource, Channel2DChart<?>, Channel2DPanel<Channel2DChart<?>>>
{
    private static final Preferences PREF = Preferences.userNodeForPackage(ImageView.class).node("ImageDialog");

    private final Action openAction = new OpenAction();

    private final FileOpeningWizard<ImageSource> openingWizard = new FileOpeningWizard<>(new OpeningModelStandard<ImageSource>(this), ImageReadingModel.getInstance());

    public ImageView(MainView parent) 
    {
        super(parent, DensityPanelFactory.getInstance(), parent.getImageHistogramDialog(), "Images", PREF, ModalityType.MODELESS);

        buildMenuBar();
        initInputAndActionMaps();
    }

    private void buildMenuBar()
    {
        JMenu fileMenu = getFileMenu();
        fileMenu.insert(new JMenuItem(openAction), 0);
    }

    private void initInputAndActionMaps()
    {
        registerActionAcceleratorKeysInInputMaps(Collections.singletonList(openAction));
    }

    @Override
    public StandardChannel2DResource copyResource(StandardChannel2DResource resourceOld, String shortName, String longName)
    {
        return new StandardChannel2DResource(resourceOld, shortName, longName);
    }

    @Override
    public StandardChannel2DResource copyResource(StandardChannel2DResource resourceOld, Set<String> typesToRetain, String shortName, String longName)
    {
        return new StandardChannel2DResource(resourceOld, typesToRetain, shortName, longName);
    }

    @Override
    public void showHistograms() 
    {
        getResultDestination().showImageHistograms(true);
    }

    @Override
    protected void close()
    {
        getResultDestination().showImages(false);
    }

    public void startPreview()
    {
        openingWizard.setVisible(true);
    }

    public void startPreview(List<ChannelSource> sources)
    {
        ConcurrentPreviewTask task = new ConcurrentPreviewTask(sources, this);          
        task.execute();
    }

    @Override
    public void requestPreviewEnd()
    {
        openingWizard.endPreview();
    }

    @Override
    public void publishPreviewed2DData(Map<StandardChannel2DResource, Map<String, Channel2DChart<?>>> charts) 
    {
        if(!charts.isEmpty())
        {
            addCharts(charts, true);            
        }       
    }

    @Override
    public List<? extends Channel2DResource> getAdditionalResources() 
    {
        return AtomicJ.getResultDestination().getMapResources();
    }

    private class OpenAction extends AbstractAction 
    {
        private static final long serialVersionUID = 1L;

        public OpenAction() 
        {
            putValue(NAME, "Open");

            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
            putValue(MNEMONIC_KEY, KeyEvent.VK_O);
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {            
            startPreview();
        }
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        // TODO Auto-generated method stub

    }
}
