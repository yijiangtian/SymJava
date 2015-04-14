package lambdacloud.core;

import symjava.bytecode.BytecodeBatchFunc;
import symjava.symbolic.Expr;
import symjava.symbolic.utils.JIT;

public class CloudVar extends Expr {
	double[] data;
	
	public CloudVar(String name) {
		this.label = name;
		this.sortKey = this.label;
	}
	
	public CloudVar(Expr expr) {
		String name = "CloudVar"+java.util.UUID.randomUUID().toString().replaceAll("-", "");
		this.label = name;
		this.sortKey = this.label;
		this.compile(name, expr);
	}
	
	public CloudVar(String name, Expr expr) {
		this.label = name;
		this.sortKey = this.label;
		this.compile(name, expr);
	}
	
	public CloudVar compile(String name, Expr expr) {
		if(CloudConfig.isLocal()) {
			BytecodeBatchFunc fexpr = JIT.compileBatchFunc(new Expr[0], expr);
			fexpr.apply(data, 0);
		} else {
			//expr contains server references
		}
		return this;
	}

	public CloudVar init(double ...array) {
		this.data = array;
		return this;
	}

	public void set(int index, double value) {
		data[index] = value;
	}
	
	public CloudVar resize(int size) {
		if(this.data == null)
			this.data = new double[size];
		else {
			double[] newdata = new double[size];
			if(size > data.length) {
				System.arraycopy(this.data, 0, newdata, 0, this.data.length);
			} else {
				System.arraycopy(this.data, 0, newdata, 0, size);
			}
			this.data = newdata;
		}
		return this;
	}
	
	public void storeToCloud() {
		
	}
	
	public double[] fetchToLocal() {
		return data;
	}
	
	@Override
	public Expr simplify() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean symEquals(Expr other) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Expr diff(Expr expr) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static CloudVar valueOf(Expr expr) {
		return new CloudVar(expr);
	}

}
