import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class MACCalculator {
	Mac mac;
	String clavDebug;
	
	public MACCalculator(String algoName,String clav) throws NoSuchAlgorithmException, InvalidKeyException {
		System.out.println(clav);
		mac = Mac.getInstance(algoName);
		clavDebug  = clav;
		byte[] decodedKey     =  clav.getBytes();
	    SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");		
		mac.init(originalKey);
	}
	
	public String calculate(String message) {
		mac.update(message.getBytes());
		byte[] res = mac.doFinal();
		System.out.println("Message: "+message+" MAC result: "+res.toString()+" SecretKey: "+clavDebug);
		return res.toString();
	}

}
