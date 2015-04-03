package symjava.examples;

import static symjava.math.SymMath.pow;
import symjava.math.SymMath;
import symjava.matrix.SymMatrix;
import symjava.matrix.SymVector;
import symjava.numeric.NumMatrix;
import symjava.numeric.NumVector;
import symjava.symbolic.Expr;
import symjava.symbolic.Sum;
import symjava.symbolic.Symbol;
import symjava.symbolic.Symbols;

public class BenchmarkRosenbrock {

	public static void test() {
		int N = 10;
		long begin, end;
		Expr rosen = null;
		Symbol i = new Symbol("i");
		Symbols xi = new Symbols("x", i);
		Symbols xim1 = new Symbols("x", i-1);
		rosen  =  Sum.apply(100*pow(xi-xim1*xim1,2) + pow(1-xim1,2), i, 2, N);
		System.out.println("Rosenbrock function with N="+N+": "+rosen);
		
		Expr[] freeVars = xi.get(1, N);
		begin = System.currentTimeMillis();
		SymVector grad = SymMath.grad(rosen);
		end = System.currentTimeMillis();
		System.out.println("Hessian Time: "+((end-begin)/1000.0));
		SymMatrix hess = SymMath.hess(rosen);
		NumVector numGrad = grad.toNumVector(freeVars);
		NumMatrix numHess = hess.toNumMatrix(freeVars);

		double[] args = new double[freeVars.length];
		System.out.println(grad);
		double[] gradResult = numGrad.eval(args);
		for(double d : gradResult)
			System.out.println(d);
		System.out.println();

		System.out.println(hess);
		double[][] hessResult = numHess.eval(args);
		for(double[] row : hessResult) {
			for(double d : row)
				System.out.print(d+" ");
			System.out.println();
		}
		
		int NN = 100;
		begin = System.currentTimeMillis();
		for(int j=0; j<NN; j++)
			numGrad.eval(args);
		end = System.currentTimeMillis();
		System.out.println("Grad Time: "+((end-begin)/1000.0));
		
		begin = System.currentTimeMillis();
		for(int j=0; j<NN; j++)
			numHess.eval(args);
		end = System.currentTimeMillis();
		System.out.println("Hessian Time: "+((end-begin)/1000.0));
		
	}
	public static void main(String[] args) {
		test();
	}

}