package lambdacloud.core;

import io.netty.channel.Channel;
import lambdacloud.core.lang.LCAssign;
import lambdacloud.net.CloudQuery;
import lambdacloud.net.CloudVarHandler;
import lambdacloud.net.CloudVarRespHandler;
import lambdacloud.net.CloudVarResp;
import lambdacloud.net.CloudClient;
import symjava.bytecode.BytecodeBatchFunc;
import symjava.symbolic.Expr;
import symjava.symbolic.Symbol;
import symjava.symbolic.utils.JIT;
import symjava.symbolic.utils.Utils;

/**
 * Cloud Shared Data (CloudSD)
 * <br>
 * An instance of CloudSD represents a shared data set on the cloud server.
 * The data set can be created on local machine and stored to the cloud side.
 * A data set on the cloud server can be download to local machine by 
 * providing its name.
 * <br>
 * 
 * priority:
 * 1. csd://ip_address/var_name
 * 2. useCloudConfig
 * 3. globalCloudConfig
 * 
 * 
 * <br>
 * Example:
 * <p><blockquote><pre>
 *     CloudSD data = new CloudSD("myvar").init(new double[]{1, 2, 3, 4, 5});
 *     data.sotoreToCloud();
 *     if(data.fetchToLocal()) {
 *       for(double d : data.getData()) {
 *         System.out.println(d);
 *       }
 *     }
 * </pre></blockquote>
 *
 */
public class CloudSD extends Symbol {
	double[] data = new double[0];
	boolean isOnCloud = false;
	protected CloudConfig localConfig = null;
	
	/**
	 * Construct a CloudSD object with random name
	 */
	public CloudSD() {
		super(generateName());
	}

	public CloudSD(String name) {
		super(name);
	}
	
	/**
	 * Construct a CloudSD object with random name
	 * based on the given expression
	 * 
	 * @param expr
	 */
	public CloudSD(Expr expr) {
		super(generateName());
		this.compile(this.label, expr);
	}
	
	public CloudSD(String name, Expr expr) {
		super(name);
		this.compile(name, expr);
	}

	public CloudSD(CloudConfig config) {
		super(generateName());
		this.localConfig = config;
	}

	public CloudSD(CloudConfig config, String name) {
		super(name);
		this.localConfig = config;
	}
	
	public CloudSD(CloudConfig config, Expr expr) {
		super(generateName());
		this.localConfig = config;
		this.compile(this.label, expr);
	}
	
	public CloudSD(CloudConfig config, String name, Expr expr) {
		super(name);
		this.localConfig = config;
		this.compile(name, expr);
	}

	public CloudSD compile(String name, Expr expr) {
		if(currentCloudConfig().isLocal()) {
			CloudSD[] args = Utils.extractCloudVars(expr).toArray(new CloudSD[0]);
			BytecodeBatchFunc fexpr = JIT.compileBatchFunc(args, expr);
			data = new double[args[0].size()];
			fexpr.apply(data, 0, Utils.getDataFromCloudVars(args));
		} else {
			//expr contains server references
		}
		return this;
	}

	/**
	 * Initialize the cloud variable with the given array.
	 * The new cloud variable simply wrap the array; that is,
	 * it is backed by the given array. Any modifications to the 
	 * cloud variable will cause the array to be modified and vice versa.
	 * @param array
	 * @return
	 */
	public CloudSD init(double ...array) {
		this.data = array;
		return this;
	}
	
	private static String generateName() {
		return "CloudSD"+java.util.UUID.randomUUID().toString().replaceAll("-", "");
	}
	
	/**
	 * Use the given cloud configuration other than the global one
	 * @param conf
	 */
	public void useCloudConfig(CloudConfig conf) {
		this.localConfig = conf;
	}
	
	/**
	 * Return current cloud configuration. Default is the global configuration.
	 * @return
	 */
	public CloudConfig currentCloudConfig() {
		if(this.localConfig != null)
			return this.localConfig;
		CloudConfig config = CloudConfig.getGlobalConfig();
		if(config == null) {
			throw new RuntimeException("CloudConfig is not specified!");
		}
		return config;
	}
	
	/**
	 * Set the value of the backed array at index
	 * @param index
	 * @param value
	 */
	public void setData(int index, double value) {
		data[index] = value;
	}
	
	/**
	 * Get the value of the backed array at index
	 * @param index
	 * @return
	 */
	public double getData(int index) {
		return data[index];
	}
	 
	/**
	 * Resize the backed array. Old data will be copied to the new backed array
	 * if the new size is larger than the old size otherwise the data that beyond
	 * the new size will be discarded.
	 * @param size
	 * @return
	 */
	public CloudSD resize(int size) {
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
	
	/**
	 * Return the length of the backed array
	 * @return
	 */
	public int size() {
		return data.length;
	}
	
	/**
	 * Return the length of the backed array
	 * @return
	 */
	public int length() {
		return data.length;
	}
	
	/**
	 * Return the name of the cloud variable. The name is the identifier
	 * of the cloud variable on the cloud server. Any local instance of 
	 * CloudSD has the same name will be assumed to be the same variable 
	 * on the cloud side.
	 * @return
	 */
	public String getName() {
		String[] arr = this.label.split("/");
		return arr[arr.length-1];
	}
	
	public String getFullName() {
		return this.label;
	}
	
	/**
	 * Return the backed array
	 * @return
	 */
	public double[] getData() {
		return data;
	}
	
	private String[] parseName(String name) {
		if(name.startsWith("csd://")) {
			String host_ip = this.label.substring(6);
			host_ip = host_ip.substring(0, host_ip.indexOf('/'));
			String[] arr = host_ip.split(":");
			if(arr.length == 2) return arr;
		}
		return null;
	}
	
	/**
	 * Store the local variable to the cloud. 
	* priority:
	* 1. csd://ip_address/var_name
	* 2. useCloudConfig
	* 3. globalCloudConfig
	 * TODO change name to store()
	 */
	public boolean storeToCloud() {
		String[] host_ip = parseName(this.getFullName());
		if(host_ip != null) {
			CloudClient c = new CloudClient(host_ip[0], Integer.valueOf(host_ip[1]));
			try {
				c.connect();
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("storeToCloud(): Connected to " + Utils.joinLabels(host_ip,":"));
			return storeToCloud(c);
		}
		CloudClient client = currentCloudConfig().currentClient();
		if(!currentCloudConfig().isLocal()) {
			return storeToCloud(client);
		} else {
			this.isOnCloud = false;
		}
		return this.isOnCloud;
	}
	
	private boolean storeToCloud(CloudClient client) {
		CloudVarRespHandler handler = client.getCloudVarRespHandler();
		try {
			client.getChannel().writeAndFlush(this).sync();
			CloudVarResp resp = handler.getCloudResp();
			if(resp.status == 0)
				this.isOnCloud = true;
			else
				this.isOnCloud = false;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Fetch a cloud variable to local. The name of the variable 
	 * on the cloud must be specified. Return true if success.
	 * Call getData() to access the data in the cloud variable
	 * TODO change name to fetch()
	 * @return
	 */
	public boolean fetchToLocal() {
		String[] host_ip = parseName(this.getFullName());
		if(host_ip != null && host_ip.length == 2) {
			CloudClient c = new CloudClient(host_ip[0], Integer.valueOf(host_ip[1]));
			try {
				c.connect();
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("fetchToLocal(): Connected to " + Utils.joinLabels(host_ip,":"));
			return fetchToLocal(c);
		}
		if(currentCloudConfig().isLocal())
			return true;
		else {
			CloudClient client = currentCloudConfig().currentClient();
			return fetchToLocal(client);
		}
	}
	
	private boolean fetchToLocal(CloudClient client) {
		Channel ch = client.getChannel();
		CloudQuery qry = new CloudQuery();
		qry.objName = this.getFullName();
		qry.qryType = CloudQuery.CLOUD_VAR;
		try {
			ch.writeAndFlush(qry).sync();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		CloudVarHandler h = client.getCloudVarHandler();
		
		//while(true) {
			CloudSD var = h.getCloudVar();
			this.data = var.data;
			this.isOnCloud = var.isOnCloud();
			if(this.data.length > 0)
				return this.isOnCloud;
			return false;
		//}
	}
	
	public boolean isOnCloud() {
		return isOnCloud;
	}
	
	public void setOnCloudFlag(boolean flag) {
		this.isOnCloud = flag;
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
	
	public static CloudSD valueOf(Expr expr) {
		return new CloudSD(expr);
	}
	
	public Expr assign(Expr expr) {
		return new LCAssign(this, expr);
	}
	
	public Expr assign(double val) {
		return new LCAssign(this, Expr.valueOf(val));
	}

	public Expr assign(int val) {
		return new LCAssign(this, Expr.valueOf(val));
	}
	
	public String toString() {
		if(this.data.length == 0)
			return this.getName()+", isOnCloud="+isOnCloud+", No data tranfered";
		else
			return this.getName()+", isOnCloud="+isOnCloud+", data.length="+this.data.length;
	}

}
