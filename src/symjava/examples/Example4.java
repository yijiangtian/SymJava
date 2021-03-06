package symjava.examples;

import static symjava.symbolic.Symbol.*;
import symjava.math.Dot;
import symjava.math.Grad;
import symjava.matrix.*;
import symjava.symbolic.*;

/**
 * Example for PDE Constrained Parameters Optimization
 * The math expression can be displayed using this online tool:
 * http://rogercortesi.com/eqn/index.php
 * or 
 * http://quicklatex.com/
 */
public class Example4 {
	public static void main(String[] args) {
		Func u =  new Func("u",  x,y,z);
		Func u0 = new Func("u0", x,y,z);
		Func q =  new Func("q",  x,y,z);
		Func q0 = new Func("q0", x,y,z);
		Func f =  new Func("f",  x,y,z);
		Func lamd = new Func("\\lambda ", x,y,z);
		
		Expr reg_term = (q-q0)*(q-q0)*0.5*0.1;

		Expr Lexpr =(u-u0)*(u-u0)/2 + reg_term + q*Dot.apply(Grad.apply(u), Grad.apply(lamd)) - f*lamd;
		//Func L = new Func("L", Lexpr);
		System.out.println("Lagrange L(u, \\lambda, q) = \n"+Lexpr);
		
		Func phi = new Func("\\phi ", x,y,z);
		Func psi = new Func("\\psi ", x,y,z);
		Func chi = new Func("\\chi ", x,y,z);
		Expr[] xs =  new Expr[]{u,   lamd, q   };
		Expr[] dxs = new Expr[]{phi, psi,  chi };
		//We want print the exact expression instead of \nebla{L}
		//SymVector Lx = Grad.apply(L, xs, dxs);
		ExprVector Lx = Grad.apply(Lexpr, xs, dxs); 
		System.out.println("\nGradient Lx = (Lu, Llamd, Lq) =");
		System.out.println(Lx.toString());
		
		Func du = new Func("\\delta{u}", x,y,z);
		Func dl = new Func("\\delta{\\lambda}", x,y,z);
		Func dq = new Func("\\delta{q}", x,y,z);
		Expr[] dxs2 = new Expr[] { du, dl, dq };
		ExprMatrix Lxx = new ExprMatrix();
		for(Expr Lxi : Lx) {
			Lxx.append(Grad.apply(Lxi, xs, dxs2));
		}
		System.out.println("\nHessian Matrix =");
		System.out.println(Lxx);
	}
}
