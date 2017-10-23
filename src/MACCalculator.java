import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class MACCalculator {
	Mac mac;
	
	public MACCalculator(String algoName,String clav) throws NoSuchAlgorithmException, InvalidKeyException {
		System.out.println(clav);
		mac = Mac.getInstance(algoName);
		byte[] decodedKey     =  Base64.getDecoder().decode(clav);
	    SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");		
		mac.init(originalKey);
	}
	
	public String calculate(String message) {
		mac.update(message.getBytes());
		byte[] res = mac.doFinal();
		return res.toString();
	}

}
