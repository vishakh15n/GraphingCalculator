package net.dvhigh.class2019.vishakhn.mathproject;

import geometry2D.MathEvaluator;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.Scanner;

/**
 * The Graphing Calculator is presented as a Window.
 * It is invoked as follows:
 *   java GraphingCalculator "<expression>" <minimum-x> <maximum-x>  <minimum-y> <maximum-y> <x-step> <y-step>
 *
 * For example, the following call plots the function "x^3 + 2x^2 - 4x + 5" from x = -5 to +10,
 * values of y shown within -50 to 1200, with x stepping at interval of 1 and y stepping at the interval of 100.
 *
 *   java GraphingCalculator "x^3 + 2 * x^2 - 4 * x + 5" -5 10 -50 1200 1 100
 *
 * You can omit any of the trailing parameters.  It will prompt you to enter those.
 *
 * The class can be used from another Java program also,  See the main() method.
 *
 * @author vishakh.nair
 */
public class GraphingCalculator extends JFrame {

    /** Stores the data for a particular X value. */
    private static class DataPoints {
        private double x;            // x
        private double y;            // f(x)
        private double yDash;        // f'(x)
        private double yDashDash;    // f"(x)
    }


    /** Indicates whether the current function is a rational polynomial. */
    private boolean rationalPolynomial;

    /** A modified version of the given class. */
    private MathEvaluator mathEvaluator;

    // Infinitesimal distance to compute limits.
    public static final double H = 0.00001;
    public static final double TWO_H = 2.0 * H;
    public static final double H_SQUARED = H * H;

    // The x-interval in which the functions are evaluated.
    private static final double DELTA = 0.01;

    // A small interval very close to zero, used to do comparisons of floating point numbers.
    private static final double EPSILON = 0.0001;

    // Properties of the line rendering main function.
    private static final Color FUNC_COLOR = Color.BLUE;
    private static final BasicStroke FUNC_STROKE = new BasicStroke(3);

    // Properties of the line rendering the first derivative.
    private static final Color DERIVATIVE_COLOR = Color.RED;
    private static final BasicStroke DERIVATIVE_STROKE = new BasicStroke(2);

    // Properties of the line rendering the second derivative.
    private static final Color SECOND_DERIVATIVE_COLOR = new Color(8,69,148);  // Dark Blue
    private static final BasicStroke SECOND_DERIVATIVE_STROKE = new BasicStroke(1);

    // Properties of the line rendering grid lines.
    private static final Color GRID_COLOR = Color.LIGHT_GRAY;
    private static final Stroke GRID_STROKE = new BasicStroke(1);

    // Properties of the line rendering X- and Y- axes.
    private static final Color AXIS_COLOR = Color.BLACK;
    private static final Stroke AXIS_STROKE = new BasicStroke(2);

    // Properties of the line rendering asymptotes.
    private static final Color ASYMPTOTE_COLOR = new Color(0, 179, 89);  // Dark Purple
    private static final Stroke ASYMPTOTE_STROKE = new BasicStroke(2, BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0) ;

    private static final Color FTC_TITLE_COLOR = Color.BLUE;
    private static final Color FTC_COLOR = Color.BLACK;


    // Other colors.

    // Color of maximum points.
    private static final Color MAX_COLOR = new Color(96,72,96);  // Dark brown

    // Color of minimum points.
    private static final Color MIN_COLOR = Color.ORANGE;

    // Color of inflection points.
    private static final Color INFLECTION_COLOR = Color.MAGENTA;

    // Color of a holes.
    private static final Color HOLE_COLOR = new Color(31, 55, 72);  // Dark Purple


    // Color of the markings on the chart.
    private static final Color LEGEND_COLOR = Color.BLACK;

    // Width of the border on all sides, in pixels.
    private static final int BORDER = 150;


    // User input variables.
    private String expression; // The plotted expression.
    private double xLow;       // Minimum x to be plotted.
    private double xHigh;      // Maximum x to be plotted.
    private double yLow;       // Minimum y to be plotted.
    private double yHigh;      // Maximum y to be plotted.
    private double xStep;      // x-interval (int chart units) where grids should be drawn.
    private double yStep;      // y-interval (int chart units) where grids should be drawn.

    // Scaling values calculated dynamically.
    private double xScale;  // Number of pixels in one x unit.
    private double yScale;  // Number of pixels in one y unit.
    private double width;   // Width of the chart in chart units.
    private double height;  // Height of the chart in chart units.

    private double xOrigin; // Distance of the Y-axis (in pixels) from the leftmost point in the chart.
    private double yOrigin; // Distance of the X-axis (in pixels) from the topmost point in the chart.

    // Fonts used for printing legends.
    private Font horizontalFont;
    private Font verticalFont;
    private Font horizontalLargeFont;

    // The Graphics object associated with the JFrame.
    private Graphics2D graphics;

    /**
     * The constructor.
     * Creates a window with 90% of the width and height of the current display, centered.
     */
    public GraphingCalculator() {
        super();

        // Title of the window.
        this.setTitle("Graphing Calculator");

        // The window has a width equal to 90% of the screen width and height equal to 90%
        // of the screen height, centered on the screen.
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double w = screenSize.width * 0.90;
        double h = screenSize.height * 0.90;
        int x = (int)(screenSize.width * 0.05);
        int y = (int)(screenSize.height * 0.05);

        this.getContentPane().setLayout(null);
        this.setBounds(x, y, (int) w, (int) h);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    /**
     * Sets parameters for plotting.
     * @param expression The algebraic expression to be plotted.
     * @param xLow The minimum value of x to be plotted.
     * @param xHigh The maximum value of x to be plotted.
     * @param yLow The minimum value of y to be plotted.
     * @param yHigh The maximum value of y to be plotted.
     * @param xStep The x interval for the grid. 0 for no grid.
     * @param yStep The y interval for the grid. 0 for no grid.
     */
    public void setValues(String expression, double xLow, double xHigh, double yLow, double yHigh,
                          double xStep, double yStep) {
        this.expression = expression;
        this.xLow = xLow;
        this.xHigh = xHigh;
        this.yLow = yLow;
        this.yHigh = yHigh;
        this.xStep = xStep;
        this.yStep = yStep;
        this.mathEvaluator = new MathEvaluator(expression);
    }

    // Called every time the window is painted or resized.
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        graphics = (Graphics2D) g;
        plot();
    }

    /**
     * Plots a function and its first and second derivatives.  Indicates minimum and maximum points.
     */
    public void plot() {

        // Leave borders at four sides.
        width = this.getWidth() - 2 * BORDER;
        height = this.getHeight() - 2 * BORDER;

        // Find scaling factors to convert chart units to pixels.
        xScale = width / (xHigh - xLow);
        yScale = height / (yHigh - yLow);

        // Find the pixel offset of the axes.
        xOrigin = -xLow * xScale + BORDER;
        yOrigin = height + BORDER + yLow * yScale;

        // Font for horizontal text.
        horizontalFont = new Font(null, Font.PLAIN, 10);

        // Font for vertical text.
        AffineTransform t = graphics.getTransform();
        t.rotate(-Math.PI/2, 0, 0);
        verticalFont = horizontalFont.deriveFont(t);

        // Font for large vertical text.
        horizontalLargeFont = new Font(null, Font.PLAIN, 15);

        // Save existing stroke and color.
        Stroke oldStroke = graphics.getStroke();
        Color oldColor = graphics.getColor();

        // Draw grid, axes and legends.
        drawGrid();

        // Compute the function and its derivatives at the leftmost point.
        double x = xLow;
        DataPoints lastDp = getDataPointsAt(x);
        x += DELTA;

        // After the function is evaluated once, we know whether it is a rational polynomial or not.
        rationalPolynomial = mathEvaluator.isRationalPolynomial();

        double firstX = lastDp.x;
        double firstY = lastDp.y;

        double area = 0.0;
        boolean areaDone = false;
        double lastX = 0.0;
        double lastY = 0.0;

        while (x <= xHigh) {
            // Compute the function and its derivatives at the next point.
            DataPoints dp = getDataPointsAt(x);

            // If the Y-value go beyond limits, use the last point as one end-point for FTC.
            if (!areaDone && (dp.y < yLow || dp.y > yHigh)) {
                lastX = lastDp.x;
                lastY = lastDp.y;
                areaDone = true;
            }

            if (rationalPolynomial) {
                MathEvaluator.Continuity c = mathEvaluator.getContinuity();
                if (c != MathEvaluator.Continuity.NORMAL && c!= MathEvaluator.Continuity.ASYMPTOTE) {
                    System.out.printf("Has a %s at (%.2f, %.2f)\n", c.name(), dp.x, dp.y);
                }
                else if (c == MathEvaluator.Continuity.ASYMPTOTE) {
                    System.out.printf("Has a %s at %.2f\n", c.name(), dp.x);
                }
                if (c == MathEvaluator.Continuity.HOLE) {
                    drawHole(dp.x, dp.y);
                } else if (c == MathEvaluator.Continuity.ASYMPTOTE) {
                    drawAsymptote(dp.x, yLow);
                }
            }

            // Draw function chart.
            graphics.setStroke(FUNC_STROKE);
            graphics.setColor(FUNC_COLOR);
            drawLine(lastDp.x, lastDp.y, x, dp.y);

            // Mark separately if it is a maximum or minimum point.
            if ((lastDp.yDash <= 0 && dp.yDash >= 0) || (lastDp.yDash >= 0 && dp.yDash <= 0) ) {
                // Derivative became zero between the two points.  Find it by linear interpolation.

                double absYDash = Math.abs(dp.yDash);
                double absLastYDash = Math.abs(lastDp.yDash);
                double ratio = absYDash / (absYDash + absLastYDash);

                double zeroX = x - ratio * (x - lastDp.x);
                Double zeroY = getFunctionValue(zeroX);

                if (dp.yDash < 0) {
                    drawMaximumDot(zeroX, zeroY);
                } else {
                    drawMinimumDot(zeroX, zeroY);
                }
            }

            // Mark separately if it is an inflection point.
            if ((lastDp.yDashDash < -EPSILON && dp.yDashDash > EPSILON) ||
                    (lastDp.yDashDash > EPSILON && dp.yDashDash < -EPSILON)) {
                // Second derivative became zero between the two points.  Find it by linear interpolation.
                double absYDashDash = Math.abs(dp.yDashDash);
                double absLastYDashDash = Math.abs(lastDp.yDashDash);
                double ratio = absYDashDash / (absYDashDash + absLastYDashDash);

                double zeroX = x - ratio * (x - lastDp.x);
                Double zeroY = getFunctionValue(zeroX);

                drawInflectionDot(zeroX, zeroY);
            }

            // Draw first derivative chart.
            if (lastDp.yDash >= yLow && lastDp.yDash <= yHigh && dp.yDash >= yLow && dp.yDash <= yHigh) {
                graphics.setStroke(DERIVATIVE_STROKE);
                graphics.setColor(DERIVATIVE_COLOR);
                drawLine(lastDp.x, lastDp.yDash, x, dp.yDash);
            }

            // Draw second derivative chart.
            if (lastDp.yDashDash >= yLow && lastDp.yDashDash <= yHigh &&
                    dp.yDashDash >= yLow && dp.yDashDash <= yHigh) {
                graphics.setStroke(SECOND_DERIVATIVE_STROKE);
                graphics.setColor(SECOND_DERIVATIVE_COLOR);
                drawLine(lastDp.x, lastDp.yDashDash, x, dp.yDashDash);
            }

            // Calculate the area under the derivative curve and add to the total area.
            if (!areaDone) {
                area += DELTA * 0.5 * (lastDp.yDash + dp.yDash);
            }

            // Move to the next point.
            lastDp = dp;
            x += DELTA;
        }

        // Draw the legend.
        drawLegend();

        if (!areaDone) {
            lastX = lastDp.x;
            lastY = lastDp.y;
        }

        // Illustrate the fundamental theorem of calculus.

        int fontX = BORDER;
        int fontY = BORDER / 2;
        graphics.setFont(horizontalLargeFont);
        FontMetrics fm = graphics.getFontMetrics();

        String title;
        String value;

        // f(a)
        graphics.setColor(FTC_TITLE_COLOR);
        title = String.format("f(%.2f) = ", firstX);
        graphics.drawString(title, fontX, fontY);
        fontX += fm.stringWidth(title);

        graphics.setColor(FTC_COLOR);
        value = String.format("%.4f", firstY);
        graphics.drawString(value, fontX, fontY);
        fontX += fm.stringWidth(value);

        // f(b)
        graphics.setColor(FTC_TITLE_COLOR);
        title = String.format(", f(%.2f) = ", lastX);
        graphics.drawString(title, fontX, fontY);
        fontX += fm.stringWidth(title);

        graphics.setColor(FTC_COLOR);
        value = String.format("%.4f", lastY);
        graphics.drawString(value, fontX, fontY);
        fontX += fm.stringWidth(value);

        // f(b) - f(a)
        graphics.setColor(FTC_TITLE_COLOR);
        title = String.format(", f(%.2f) - f(%.2f) = ", lastX, firstX);
        graphics.drawString(title, fontX, fontY);
        fontX += fm.stringWidth(title);

        graphics.setColor(FTC_COLOR);
        value = String.format("%.4f", lastY - firstY);
        graphics.drawString(value, fontX, fontY);
        fontX += fm.stringWidth(value);

        // Area under derivative.
        graphics.setColor(FTC_TITLE_COLOR);
        title = ", Area under derivative = ";
        graphics.drawString(title, fontX, fontY);
        fontX += fm.stringWidth(title);

        graphics.setColor(FTC_COLOR);
        value = String.format("%.4f", area);
        graphics.drawString(value, fontX, fontY);

        // Restore old stroke and color.
        graphics.setStroke(oldStroke);
        graphics.setColor(oldColor);

    }

    private void drawAsymptote(double x, double y) {
        graphics.setColor(ASYMPTOTE_COLOR);
        graphics.setStroke(ASYMPTOTE_STROKE);
        drawLine(x, yLow, x, yHigh);
        graphics.drawString("x", xToPixels(x), yToPixels(y));
    }

    private void drawHole(double x, double y) {
        drawColoredDot(x, y, HOLE_COLOR, true);
    }

    private void drawHoleNoCheck(double x, double y) {
        drawColoredDot(x, y, HOLE_COLOR, false);
    }

    private Double getFunctionValue(double x) {
        mathEvaluator.addVariable("x", x);
        return mathEvaluator.getValue();
    }

    private void drawMaximumDot(double x, double y) {
        drawColoredDot(x, y, MAX_COLOR, true);
    }

    private void drawMaximumDotNoCheck(double x, double y) {
        drawColoredDot(x, y, MAX_COLOR, false);
    }

    private void drawMinimumDot(double x, double y) {
        drawColoredDot(x, y, MIN_COLOR, true);
    }

    private void drawMinimumDotNoCheck(double x, double y) {
        drawColoredDot(x, y, MIN_COLOR, false);
    }

    private void drawInflectionDot(double x, Double y) {
        drawColoredDot(x, y, INFLECTION_COLOR, true);
    }

    private void drawInflectionDotNoCheck(double x, Double y) {
        drawColoredDot(x, y, INFLECTION_COLOR, false);
    }

    private void drawColoredDot(double x, Double y, Color color, boolean checkBounds) {
        if (checkBounds && (y < yLow || y > yHigh)) {
            return;
        }
        graphics.setColor(color);
        Ellipse2D.Double circle = new Ellipse2D.Double(xToPixels(x) - 5, yToPixels(y) - 5, 10, 10);
        graphics.fill(circle);
    }

    /**
     * Computes f(x), f'(x) and f''(x) at an x-value x.
     * @param x The x value.
     * @return An object whose fields give values of the expression, its derivative and its second derivative.
     */
    private DataPoints getDataPointsAt(double x) {
        DataPoints result = new DataPoints();

        result.x = x;

        // Compute f(x).
        result.y = getFunctionValue(x);

        // Compute f(x+h).
        Double fXPlusH = getFunctionValue(x + H);

        // Compute f(x-h).
        Double fXMinusH = getFunctionValue(x - H);

        // Compute f'(x).
        result.yDash = (fXPlusH - fXMinusH) / TWO_H;

        // Compute f''(x).
        result.yDashDash = (fXPlusH - 2 * result.y + fXMinusH) / H_SQUARED;

        return result;
    }

    /**
     * Draws a line on the graphics pane, using the current stroke and color.
     * Makes sure the points lie in the chart boundaries.
     * @param x1 Starting x co-ordinate, in chart units.
     * @param y1 Starting y co-ordinate, in chart units.
     * @param x2 Ending x co-ordinate, in chart units.
     * @param y2 Ending y co-ordinate, in chart units.
     */
    private void drawLine(double x1, double y1, double x2, double y2) {
        drawLineSegment(x1, y1, x2, y2, true);
    }

    /** Same as drawLine(), but with no check for boundaries. */
    private void drawLineNoCheck(double x1, double y1, double x2, double y2) {
        drawLineSegment(x1, y1, x2, y2, false);
    }

    private void drawLineSegment(double x1, double y1, double x2, double y2, boolean checkBounds) {
        if (checkBounds) {
            if ((y1 < yLow || y1 > yHigh) && (y2 < yLow || y2 > yHigh)) {
                return;
            }
            if (y1 < yLow) {
                x1 = x2 + (yLow - y2) * (x2 - x1)/ (y2 - y1);
                y1 = yLow;
            } else if (y2 < yLow) {
                x2 = x1 - (yLow - y1) * (x2 - x1)/ (y2 - y1);
                y2 = yLow;
            } else if (y1 > yHigh) {
                x1 = x2 + (yHigh - y2) * (x2 - x1)/ (y2 - y1);
                y1 = yHigh;
            } else if (y2 > yHigh) {
                x2 = x1 + (yHigh - y1) * (x2 - x1)/ (y2 - y1);
                y2 = yHigh;
            }
        }
        Line2D line = new Line2D.Double(xToPixels(x1),  yToPixels(y1), xToPixels(x2), yToPixels(y2));
        graphics.draw(line);
    }

    /** Checks whether a floating point value is close enough to zero to be considered as zero. */
    private static boolean isZero(double value) {
        return Math.abs(value) < EPSILON;
    }

    /**
     * Draws the grids for the chart.
     */
    private void drawGrid() {
        graphics.setStroke(GRID_STROKE);
        graphics.setColor(GRID_COLOR);
        double x;
        for (x = xLow; x < xHigh; x += xStep) {
            drawLine(x, yLow, x, yHigh);
        }
        drawLine(xHigh, yLow, xHigh, yHigh);

        for (double y = yLow; y < yHigh; y += yStep) {
            drawLine(xLow, y, xHigh, y);
        }
        drawLine(xLow, yHigh, xHigh, yHigh);

        graphics.setColor(AXIS_COLOR);
        graphics.setStroke(AXIS_STROKE);
        drawLine(xLow, 0, xHigh, 0);
        drawLine(0, yLow, 0, yHigh);
    }

    /**
     * Converts X co-ordinate to pixels.
     * @param x The X co-ordinate.
     * @return Number of pixels from the left side of the window.
     */
    private int xToPixels(double x) {
        return (int) (xOrigin + xScale * x + 0.5);
    }

    /**
     * Converts X co-ordinate to pixels.
     * @param y The Y co-ordinate.
     * @return Number of pixels from the top of the window.
     */
    private int yToPixels(double y) {
        return (int) (yOrigin - yScale * y + 0.5);
    }

    /**
     * Draws the legend around the chart.
     */
    private void drawLegend() {
        double x;
        graphics.setColor(LEGEND_COLOR);

        Font originalFont = graphics.getFont();
        graphics.setFont(verticalFont);
        for (x = xLow; x <= xHigh; x += xStep) {
            graphics.drawString(String.format("%10.1f", x), xToPixels(x), (int) (BORDER + height + 50));
        }
        graphics.drawString(String.format("%6.2f", 0.0),
                (int) (xOrigin), (int) (BORDER));

        graphics.setFont(horizontalFont);
        for (double y = yLow; y <= yHigh + EPSILON; y += yStep) {
            graphics.drawString(String.format("%10.1f", y), 100, yToPixels(y));
        }
        graphics.drawString(String.format("%6.2f", 0.0), (int) (this.getWidth() - BORDER), (int) yOrigin);

        ////// Print the legend at the bottom.

        double xLeft = xLow + 0.20 * (xHigh - xLow);
        double yLeft = yLow - 60.0 / yScale;

        //// Left side.

        // Print f(x).
        graphics.setColor(FUNC_COLOR);
        graphics.setStroke(FUNC_STROKE);
        drawLineNoCheck(xLow, yLeft, xLeft, yLeft);
        graphics.setColor(LEGEND_COLOR);
        graphics.drawString("f(x) = " + expression, xToPixels(xLeft) + 20, yToPixels(yLeft));

        // Print f'(x).
        yLeft -= 20.0 / yScale;
        graphics.setColor(DERIVATIVE_COLOR);
        graphics.setStroke(DERIVATIVE_STROKE);
        drawLineNoCheck(xLow, yLeft, xLeft, yLeft);
        graphics.setColor(LEGEND_COLOR);
        graphics.drawString("f'(x)", xToPixels(xLeft) + 20, yToPixels(yLeft));

        // Print f''(x).
        yLeft -= 20.0 / yScale;
        graphics.setColor(SECOND_DERIVATIVE_COLOR);
        graphics.setStroke(SECOND_DERIVATIVE_STROKE);
        drawLineNoCheck(xLow, yLeft, xLeft, yLeft);
        graphics.setColor(LEGEND_COLOR);
        graphics.drawString("f''(x)", xToPixels(xLeft) + 20, yToPixels(yLeft));

        if (rationalPolynomial) {
            // Print Asymptote.
            yLeft -= 20.0 / yScale;
            graphics.setColor(ASYMPTOTE_COLOR);
            graphics.setStroke(ASYMPTOTE_STROKE);
            drawLineNoCheck(xLow, yLeft, xLeft, yLeft);
            graphics.setColor(LEGEND_COLOR);
            graphics.drawString("Asymptote", xToPixels(xLeft) + 20, yToPixels(yLeft));

        }

        //// Right side.

        double xRight = xLow + (xHigh - xLow) * 0.75;
        double yRight = yLow - 60.0 / yScale;

        // Print Maximum.
        drawMaximumDotNoCheck(xRight, yRight);
        graphics.setColor(LEGEND_COLOR);
        graphics.drawString("Maximum point", xToPixels(xRight) + 20, yToPixels(yRight));

        // Print Minimum.
        yRight -= 20.0 / yScale;
        drawMinimumDotNoCheck(xRight, yRight);
        graphics.setColor(LEGEND_COLOR);
        graphics.drawString("Minimum point", xToPixels(xRight) + 20, yToPixels(yRight));

        // Print Inflection.
        yRight -= 20.0 / yScale;
        drawInflectionDotNoCheck(xRight, yRight);
        graphics.setColor(LEGEND_COLOR);
        graphics.drawString("Inflection point", xToPixels(xRight) + 20, yToPixels(yRight));

        if (rationalPolynomial) {
            // Print Asymptote.
            yRight -= 20.0 / yScale;
            graphics.setColor(HOLE_COLOR);
            drawHoleNoCheck(xRight, yRight);
            graphics.setColor(LEGEND_COLOR);
            graphics.drawString("Hole", xToPixels(xRight) + 20, yToPixels(yRight));

        }

        graphics.setFont(originalFont);
    }

    /** The App. */
    public static void main(String[] args) {

        Scanner in = new Scanner(System.in);

        String expression;
        if (args.length < 1) {
            System.out.print("Enter the expression                       : ");
            expression = in.nextLine().trim();
        } else {
            expression = args[0];
        }

        double xLow;
        if (args.length < 2) {
            System.out.print("Enter the minimum value of x to be plotted : ");
            xLow = in.nextDouble();
        } else {
            xLow = Double.parseDouble(args[1]);
        }

        double xHigh;
        if (args.length < 3) {
            System.out.print("Enter the maximum value of x to be plotted : ");
            xHigh = in.nextDouble();
        } else {
            xHigh = Double.parseDouble(args[2]);
        }

        double yLow;
        if (args.length < 4) {
            System.out.print("Enter the minimum value of y to be plotted : ");
            yLow = in.nextDouble();
        } else {
            yLow = Double.parseDouble(args[3]);
        }

        double yHigh;
        if (args.length < 5) {
            System.out.print("Enter the maximum value of y to be plotted : ");
            yHigh = in.nextDouble();
        } else {
            yHigh = Double.parseDouble(args[4]);
        }

        double xStep;
        if (args.length < 6) {
            System.out.print("Enter the x interval for the grid          : ");
            xStep = in.nextDouble();
        } else {
            xStep = Double.parseDouble(args[5]);
        }

        double yStep;
        if (args.length < 7) {
            System.out.print("Enter the y interval for the grid          : ");
            yStep = in.nextDouble();
        } else {
            yStep = Double.parseDouble(args[6]);
        }

        GraphingCalculator gc = new GraphingCalculator();
        gc.setValues(expression, xLow, xHigh, yLow, yHigh, xStep, yStep);
        gc.setVisible(true);
    }

}
