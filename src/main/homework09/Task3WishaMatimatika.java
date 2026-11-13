package semester1.practice09.homework;

import java.util.Arrays;

public class Task3NumericalCalculusEngine {
    public static void main(String[] args) {
        System.out.println("================ ЛАБОРАТОРІЯ ЧИСЕЛЬНОГО АНАЛІЗУ ================");

        double[] polyCoeffs = {1.0, 0.0, 1.0};
        AbstractFunction1D g = new PolynomialFunction(polyCoeffs);

        System.out.printf("1. Многочлен g(x) = %s\n", g.getExpressionName());
        System.out.printf("   Значення g(2.0) = %.2f\n", g.evaluate(2.0));
        System.out.printf("   Аналітична похідна g'(2.0) = %f | Чисельна = %f (Похибка: %.2e)\n",
                g.analyticDerivative(2.0), g.numericalDerivative(2.0), g.getDerivativeError(2.0));
        System.out.printf("   Інтеграл на [0.00; 3.00] (метод трапецій, N=1000): %.4f (Точне значення: 12.0)\n\n",
                g.integrateTrapezoidal(0.0, 3.0, 1000));

        AbstractFunction1D f = new SineWaveFunction(2.0, 1.0, 0.0);
        System.out.printf("2. Гармонічна функція f(x) = %s\n", f.getExpressionName());
        System.out.printf("   Інтеграл на [0.00; 3.1416] (N=1000): %.4f\n\n",
                f.integrateTrapezoidal(0.0, Math.PI, 1000));

        AbstractFunction1D H = new CompositeFunction1D(f, g);
        double pointX = 1.5;

        System.out.println("3. Суперпозиція (Композиція об'єктів) H(x) = f(g(x)) = 2*sin(x^2 + 1):");
        System.out.printf("   Точка x = %.2f:\n", pointX);
        System.out.printf("   -> H(%.2f) = 2 * sin(%.2f) = %f\n", pointX, g.evaluate(pointX), H.evaluate(pointX));
        System.out.printf("   -> Аналітична похідна H'(%.2f) за правилом ланцюга: %f\n", pointX, H.analyticDerivative(pointX));
        System.out.printf("   -> Чисельна похідна H'(%.2f) через кінцеву різницю: %f\n", pointX, H.numericalDerivative(pointX));
        System.out.printf("   -> Розбіжність між формулами: %.2e (ІДЕАЛЬНИЙ ЗБІГ!)\n", H.getDerivativeError(pointX));

        System.out.println("================================================================");
    }
}

abstract class AbstractFunction1D {
    private final String expressionName;

    public AbstractFunction1D(String expressionName) {
        this.expressionName = expressionName;
    }

    public String getExpressionName() {
        return expressionName;
    }

    public abstract double evaluate(double x);
    public abstract double analyticDerivative(double x);

    public final double numericalDerivative(double x) {
        double h = 1e-5;
        return (evaluate(x + h) - evaluate(x - h)) / (2 * h);
    }

    public final double getDerivativeError(double x) {
        return Math.abs(analyticDerivative(x) - numericalDerivative(x));
    }

    public final double integrateTrapezoidal(double a, double b, int intervals) {
        if (intervals <= 0) {
            intervals = 1000;
        }
        double deltaX = (b - a) / intervals;
        double sum = (evaluate(a) + evaluate(b)) / 2.0;

        for (int i = 1; i < intervals; i++) {
            sum += evaluate(a + i * deltaX);
        }

        return deltaX * sum;
    }

    @Override
    public String toString() {
        return expressionName;
    }
}

class PolynomialFunction extends AbstractFunction1D {
    private final double[] coeffs;

    public PolynomialFunction(double[] coefficients) {
        super(buildPolynomialString(coefficients));
        this.coeffs = coefficients != null ? coefficients.clone() : new double[]{0};
    }

    public double[] getCoefficients() {
        return coeffs.clone();
    }

    @Override
    public double evaluate(double x) {
        double sum = 0;
        for (int i = 0; i < coeffs.length; i++) {
            sum += coeffs[i] * Math.pow(x, i);
        }
        return sum;
    }

    @Override
    public double analyticDerivative(double x) {
        double sum = 0;
        for (int i = 1; i < coeffs.length; i++) {
            sum += i * coeffs[i] * Math.pow(x, i - 1);
        }
        return sum;
    }

    private static String buildPolynomialString(double[] coefficients) {
        if (coefficients == null || coefficients.length == 0) return "0.00";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < coefficients.length; i++) {
            if (i == 0) {
                sb.append(String.format("%.2f", coefficients[i]));
            } else {
                sb.append(String.format(" + %.2f*x^%d", coefficients[i], i));
            }
        }
        return sb.toString().replace("x^1 ", "x ");
    }
}

class SineWaveFunction extends AbstractFunction1D {
    private final double amplitude;
    private final double omega;
    private final double phase;

    public SineWaveFunction(double amplitude, double omega, double phase) {
        super(String.format("%.2f*sin(%.2f*x + %.2f)", amplitude, omega, phase));
        this.amplitude = amplitude;
        this.omega = omega;
        this.phase = phase;
    }

    @Override
    public double evaluate(double x) {
        return amplitude * Math.sin(omega * x + phase);
    }

    @Override
    public double analyticDerivative(double x) {
        return amplitude * omega * Math.cos(omega * x + phase);
    }
}

class CompositeFunction1D extends AbstractFunction1D {
    private final AbstractFunction1D outer;
    private final AbstractFunction1D inner;

    public CompositeFunction1D(AbstractFunction1D outer, AbstractFunction1D inner) {
        super("(" + outer.getExpressionName() + " ∘ " + inner.getExpressionName() + ")");
        this.outer = outer;
        this.inner = inner;
    }

    @Override
    public double evaluate(double x) {
        return outer.evaluate(inner.evaluate(x));
    }

    @Override
    public double analyticDerivative(double x) {
        return outer.analyticDerivative(inner.evaluate(x)) * inner.analyticDerivative(x);
    }
}
