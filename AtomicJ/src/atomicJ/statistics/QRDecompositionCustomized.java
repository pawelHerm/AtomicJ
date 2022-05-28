package atomicJ.statistics;

import java.util.Arrays;

import Jama.Matrix;
import Jama.util.*;

/** QR Decomposition.
<P>
   For an m-by-n matrix A with m >= n, the QR decomposition is an m-by-n
   orthogonal matrix Q and an n-by-n upper triangular matrix R so that
   A = Q*R.
<P>
   The QR decompostion always exists, even if the matrix does not have
   full rank, so the constructor will never fail.  The primary use of the
   QR decomposition is in the least squares solution of nonsquare systems
   of simultaneous linear equations.  This will fail if isFullRank()
   returns false.
 */

public class QRDecompositionCustomized implements java.io.Serializable {

    /* ------------------------
   Class variables
     * ------------------------ */

    /** Array for internal storage of decomposition.
   @serial internal array storage.
     */
    private final double[][] QR;

    /** Row and column dimensions.
   @serial column dimension.
   @serial row dimension.
     */
    private final int m, n;

    /** Array for internal storage of diagonal of R.
   @serial diagonal of R.
     */
    private final double[] Rdiag;

    /* ------------------------
   Constructor
     * ------------------------ */

    /** QR Decomposition, computed by Householder reflections.
   @param A    Rectangular matrix
   @return     Structure to access R and the Householder vectors and compute Q.
     */


    public QRDecompositionCustomized (Matrix A)
    {    
        this(A.getArray(), A.getRowDimension(), A.getColumnDimension());
    }

    public QRDecompositionCustomized(double[][] A, int rowCount, int columnCount)
    {
        // Initialize.
        QR = A;
        m = rowCount;
        n = columnCount;
        Rdiag = new double[n];

        // Main loop.
        for (int k = 0; k < n; k++) {
            // Compute 2-norm of k-th column without under/overflow.
            double nrm = 0;
            for (int i = k; i < m; i++) {
                nrm = Maths.hypot(nrm,QR[i][k]);
            }

            if (nrm != 0.0) {
                // Form k-th Householder vector.
                if (QR[k][k] < 0) {
                    nrm = -nrm;
                }
                for (int i = k; i < m; i++) {
                    QR[i][k] /= nrm;
                }
                QR[k][k] += 1.0;

                // Apply transformation to remaining columns.
                for (int j = k+1; j < n; j++) {
                    double s = 0.0; 
                    for (int i = k; i < m; i++) {
                        s += QR[i][k]*QR[i][j];
                    }
                    s = -s/QR[k][k];
                    for (int i = k; i < m; i++) {
                        QR[i][j] += s*QR[i][k];
                    }
                }
            }
            Rdiag[k] = -nrm;
        }
    }

    /** Is the matrix full rank?
   @return     true if R, and hence A, has full rank.
     */

    public boolean isFullRank () {
        for (int j = 0; j < n; j++) {
            if (Rdiag[j] == 0)
                return false;
        }
        return true;
    }



    public double[] solveWithColumn (double[] B) 
    {   
        double[] X = Arrays.copyOf(B, B.length);

        // Compute Y = transpose(Q)*B
        for (int k = 0; k < n; k++)
        {
            double s = 0.0; 
            for (int i = k; i < m; i++)
            {
                s += QR[i][k]*X[i];
            }
            s = -s/QR[k][k];
            for (int i = k; i < m; i++)
            {
                X[i] += s*QR[i][k];
            }
        }
        // Solve R*X = Y;
        for (int k = n-1; k >= 0; k--)
        {
            X[k] /= Rdiag[k];

            for (int i = 0; i < k; i++)
            {
                X[i] -= X[k]*QR[i][k];
            }
        }

        return X;
    }
}
