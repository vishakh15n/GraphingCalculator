package net.dvhigh.class2019.vishakhn.mathproject;

import java.util.Scanner;

public class GraphingCalculatorTest {

    private static class Parameters {
        String expr;
        double xLow;
        double xHigh;
        double yLow;
        double yHigh;
        double xStep;
        double yStep;

        public Parameters(String expr, double xLow, double xHigh, double yLow, double yHigh, double xStep, double yStep) {
            this.expr = expr;
            this.xLow = xLow;
            this.xHigh = xHigh;
            this.yLow = yLow;
            this.yHigh = yHigh;
            this.xStep = xStep;
            this.yStep = yStep;
        }
    }

    public static void main(String[] args) {

        GraphingCalculatorTest.Parameters[] data = {
                new Parameters("sin(x)", -3 * Math.PI, 3 * Math.PI, -1.5, 1.5,
                        0.5 * Math.PI, 0.25),
                new Parameters("x^2 - 2*x + 3", -5, 5, -10, 30, 1, 5),
                new Parameters("(x^3 - 4*x^2 + 5 * x + 4)/(x-2)", -5, 5, -300, 300, 1, 100),
                new Parameters("x^3 - 3*x^2 - 144*x + 432", -13, 13,-500, 1000, 1, 100 ),
                new Parameters("(x^3 - 1)/(x-1)", -5, 5,-3, 30, 1, 5 ),
                new Parameters("tan(x)", -2.5 * Math.PI, 2.5 * Math.PI, -10, 10, 0.5 * Math.PI, 1),
                new Parameters("2 * x + 1", -10, 10, -20, 20, 1, 4)

        };


        Scanner in = new Scanner(System.in);

        for (int i = 0; i < data.length; ++i) {
            System.out.printf("%2d : %s [%.2f - %.2f]\n", i + 1, data[i].expr, data[i].xLow, data[i].xHigh);
        }
        int choice = data.length + 1;
        while (choice < 1 || choice > data.length) {
            System.out.print("Enter choice : ");
            choice = in.nextInt();
        }
        Parameters chosenData = data[choice - 1];
        GraphingCalculator gc = new GraphingCalculator();
        gc.setValues(chosenData.expr, chosenData.xLow, chosenData.xHigh, chosenData.yLow, chosenData.yHigh,
                chosenData.xStep, chosenData.yStep);
        gc.setVisible(true);


    }
}
