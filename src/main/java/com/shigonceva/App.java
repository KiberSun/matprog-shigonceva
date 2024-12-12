package com.shigonceva;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        final double[][] A = {
                {1, 2},
                {3, 1},
                {2, 3},
                {1, 2},
                {3, 1},
                {2, 3},
                {1, 1},
                {2, 2},
        };

        final double[] L = {4, 5};
        final double[] C = {2, 3, 2, 1, 2, 3, 1, 2};

        int n = A.length;
        int m = L.length;
        for (double[] row : A) {
            if (row.length < m) m = row.length;
        }

        System.out.println("Количество предприятий: " + n);
        System.out.println("Количество товаров: " + m);

        new SimplexTable(m, n, A, L, C).solve().printAnswer();

    }
}
