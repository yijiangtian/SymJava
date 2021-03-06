package lambdacloud.net;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import lambdacloud.core.CloudSD;

public class NetIOUtils {
	public static CloudSD createCloudSD(byte[] data, int nameLen, int dataLen) {
		String name = null;
		try {
			name = new String(data, 0, nameLen, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		double[] ddata = toDoubleArray(data, nameLen, dataLen);
		return new CloudSD(name).init(ddata);
	}
	
	public static CloudSDResp createCloudSDResp(byte[] data, int status, int nameLen, int messageLen) {
		CloudSDResp resp = new CloudSDResp();
		resp.status = status;
		try {
			resp.objName = new String(data, 0, nameLen, "UTF-8");
			resp.message = new String(data, nameLen, messageLen, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return resp;
	}
	
	public static CloudFuncResp createCloudFuncResp(byte[] data, int status, int nameLen, int messageLen) {
		CloudFuncResp resp = new CloudFuncResp();
		resp.status = status;
		try {
			resp.objName = new String(data, 0, nameLen, "UTF-8");
			resp.message = new String(data, nameLen, messageLen, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return resp;
	}
	
	public static double[] toDoubleArray(byte[] byteArray, int offset, int length){
	    int times = Double.SIZE / Byte.SIZE;
	    double[] doubles = new double[length / times];
	    for(int i=0;i<doubles.length;i++){
	        doubles[i] = ByteBuffer.wrap(byteArray, offset+i*times, times).getDouble();
	    }
	    return doubles;
	}
	
}
