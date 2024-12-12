package com.shigonceva;

import java.util.ArrayList;
import java.util.List;

public class SimplexTable {

    private int width;
    private int height;
    int m, n;
    private Index[] horizontalIndexes;
    private Index[] verticalIndexes;
    private double[][] originalData;
    private double[][] data;
    private double[][] recalculatingTable;
    private boolean solved = false;
    List <Result> results;



    // Первый индекс - номер строки (vertical), второй - номер столбца (horizontal)
    // M - количество заводов, n - количество продуктов
    // M * n - количество столбцов
    // M + n - 1 - количество ограничений (m и n - 1)
    // M + n - количество строк в data (M + n - 1) - L


    SimplexTable(int m, int n, double[][] A, double[] L, double[] C) {
        horizontalIndexes = new Index[m * n + 1];
        for (int i = 1; i <= n; i++)
            for (int j = 1; j <= m; j++)
                horizontalIndexes[(i - 1) * m + j - 1] = new Index('X', i, j);
        horizontalIndexes[m * n] = new Index('B', 0, 0);

        verticalIndexes = new Index[m + n];
        for (int i = 1; i <= verticalIndexes.length; i++)
            verticalIndexes[i - 1] = new Index('Y', i, 0);
        verticalIndexes[verticalIndexes.length - 1] = new Index('L', 0, 0);

        data = new double[verticalIndexes.length][horizontalIndexes.length];
        recalculatingTable = new double[verticalIndexes.length][horizontalIndexes.length];

        for (int i = 0; i < m * n; i++){
            for (int j = 0; j < n; j++){
                data[j][i] = horizontalIndexes[i].i() == j + 1 ? 1 : 0;
            }
            for (int j = n; j < n + m - 1; j++){
                Index horizontalIndex = horizontalIndexes[i];
                int verticalIndex = j - n + 2;

                data[j][i] = horizontalIndex.j() == 1 ? A[horizontalIndex.i() - 1][0] : (
                        verticalIndex == horizontalIndex.j() ?
                                - L[0] / L[verticalIndex - 1] * A[horizontalIndex.i() - 1][horizontalIndex.j() - 1] : 0
                        );
            }
            data[m + n - 1][i] = horizontalIndexes[i].j() == 1 ? -A[horizontalIndexes[i].i() - 1][horizontalIndexes[i].j() - 1] : 0;
        }



        for (int i = 0; i < verticalIndexes.length; i++)
            data[i][m * n] = i < n ? C[i] : 0;

//        for (int j = 0; j < n + m; j++){
//            for (int i = 0; i < m * n; i++){
//                System.out.print(data[i][j] + "\t");
//            }
//            System.out.println(B[j]);
//        }

        this.m = m;
        this.n = n;
        originalData = new double[verticalIndexes.length][horizontalIndexes.length];
        for (int i = 0; i < m + n; i++){
            for (int j = 0; j < m * n + 1; j++){
                originalData[i][j] = data[i][j];
            }
        }
    }

    public SimplexTable solve(){
        while (true){
            double min = 0;
            int minIndex = 0;
            for (int i = 0; i < horizontalIndexes.length; i++){
                if (data[m + n - 1][i] < min){
                    min = data[m + n - 1][i];
                    minIndex = i;
                }
            }
            if (min > -0.000001 || solved) break;
            recalculate(minIndex);
        }
        landDoig();
        return this;
    }

    private void recalculate(int column) {
        double min = -1;
        int row = -1;
        for (int i = 0; i < m + n - 1; i++){
            if (data[i][column] < 0.000001) continue;
            if ((data[i][m * n] / data[i][column] < min) || (row == -1)){
                min = data[i][m * n] / data[i][column];
                row = i;
            }
        }

        if (min == -1) {
            System.out.println("Оптимального решения не существует");
            this.solved = true;
            return;
        }


        double lambda = recalculatingTable[row][column] = 1/data[row][column];
        for (int i = 0; i < m + n; i++){
            if (i == row) continue;
            recalculatingTable[i][column] = -data[i][column]*lambda;
        }
        for (int i = 0; i < horizontalIndexes.length; i++){
            if (i == column) continue;
            recalculatingTable[row][i] = data[row][i]*lambda;
        }
        for (int i = 0; i < verticalIndexes.length; i++){
            for (int j = 0; j < horizontalIndexes.length; j++){
                if ((i == row) || (j == column)) continue;
                recalculatingTable[i][j] = recalculatingTable[i][column] * data[row][j];
            }
        }
        for (int i = 0; i < verticalIndexes.length; i++){
            for (int j = 0; j < horizontalIndexes.length; j++){
                if ((i == row) || (j == column)) {
                    data[i][j] = recalculatingTable[i][j];
                } else data[i][j] += recalculatingTable[i][j];
            }
        }
        Index tempIndex = horizontalIndexes[column];
        horizontalIndexes[column] = verticalIndexes[row];
        verticalIndexes[row] = tempIndex;
    }

    private void landDoig(){
        results = new ArrayList<>(verticalIndexes.length);
        for (int i = 0; i < verticalIndexes.length; i++){
            if (verticalIndexes[i].letter() == 'X')
                results.add(new Result(verticalIndexes[i], data[i][m * n]));
        }
int p = 1 + 1;
        List <List <Result>> possibleResults = new ArrayList<>();
        possibleResults.add(results);
        for (int i = 0; i < results.size(); i++){
            if (results.get(i).value() - Math.floor(results.get(i).value()) > 0.00001){
                List<List<Result>> tempResults = new ArrayList<>();
                for (int j = 0; j < possibleResults.size(); j++){
                    tempResults.add(new ArrayList<>());
                    for (int k = 0; k < possibleResults.get(j).size(); k++){
                        tempResults.get(j).add(new Result(possibleResults.get(j).get(k).index(), possibleResults.get(j).get(k).value()));
                    }
                }
                possibleResults.addAll(tempResults);
                for (int j = 0; j < possibleResults.size() / 2; j++){
                    Result newResult = new Result(possibleResults.get(j).get(i).index(),
                            Math.floor(possibleResults.get(j).get(i).value()));
                    possibleResults.get(j).set(i, newResult);
                }
                for (int j = possibleResults.size() / 2; j < possibleResults.size(); j++){
                    Result newResult = new Result(possibleResults.get(j).get(i).index(),
                            Math.ceil(possibleResults.get(j).get(i).value()));
                    possibleResults.get(j).set(i, newResult);
                }
            }
        }
        List <Double> rates = new ArrayList<>();
        for (int i = 0; i < possibleResults.size(); i++){
            rates.add(rate(possibleResults.get(i)));
        }
        int minIndex = 0;
        Double minValue = Double.POSITIVE_INFINITY;
        for (int i = 0; i < possibleResults.size(); i++){
            if (rates.get(i) < minValue){
                minValue = rates.get(i);
                minIndex = i;
            }
        }
        results = possibleResults.get(minIndex);
    }

    private double rate(List <Result> results){
        double rate = 0;
        for (int i = 0 ; i < m + n - 1; i++){
            double constrain = 0;
            for (int j = 0; j < results.size(); j++){
                Index index = results.get(j).index();
                constrain += results.get(j).value() * originalData[i][(index.i() - 1) * m + index.j() - 1];
            }
            boolean cond = constrain > originalData[i][m * n] - 0.00001;
            if (constrain > originalData[i][m * n] + 0.00001) return Double.POSITIVE_INFINITY;
        }
        for (int i = 0; i < results.size(); i++){
            Index index = results.get(i).index();
            rate += results.get(i).value() * originalData[m + n - 1][(index.i() - 1) * m + index.j() - 1];
        }
        return rate;
    }

//    public void print(){
//        for (int i = 0; i < horizontalIndexes.length; i++){
//            System.out.println(horizontalIndexes[i]);
//        }
//        System.out.println();
//        for (int i = 0; i < verticalIndexes.length; i++){
//            System.out.println(verticalIndexes[i]);
//        }
//        System.out.println();
//        for (int i = 0; i < verticalIndexes.length; i++){
//            for (int j = 0; j < horizontalIndexes.length; j++){
//                System.out.print(data[i][j] + " ");
//            }
//            System.out.println();
//        }
//    }

    public void printResult(){
        List <Result> results = new ArrayList<>(verticalIndexes.length);

        for (int i = 0; i < verticalIndexes.length; i++){
            if (verticalIndexes[i].letter() == 'X')
                results.add(new Result(verticalIndexes[i], data[i][m * n]));
//                System.out.println("X " + verticalIndexes[i].i() + " " + verticalIndexes[i].j() + " = " + data[i][m * n]);
        }
        results.sort((o1, o2) -> {
            if (o1.index().i() < o2.index().i()) return -1;
            if (o1.index().i() > o2.index().i()) return 1;
            return Integer.compare(o1.index().j(), o2.index().j());
        });

        for (Result result : results) {
            System.out.println("X " + result.index().i() + " " + result.index().j() + " = " + result.value());
        }
    }

    public void printAnswer(){
        results.sort((o1, o2) -> {
            if (o1.index().i() < o2.index().i()) return -1;
            if (o1.index().i() > o2.index().i()) return 1;
            return Integer.compare(o1.index().j(), o2.index().j());
        });

        for (Result result : results) {
            if (result.value() < 1) continue;
            System.out.println("Предприятию " + result.index().i() + " следует производить товар " + result.index().j() + " на " + (int) Math.floor(result.value()) + " заводах");
        }
    }

}
