package online.buzzzz.security;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import android.util.Base64;

public class AESCrypto {

    private static final int KEYLENGTH = 16;
    private static final int IVLENGTH = 16;
    private static final String ALGORITHM = "AES/CBC/PKCS5PADDING";
    private static final char[] DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    private static class AESCryptoException extends RuntimeException{
        public AESCryptoException(){
            super();
        }

        private AESCryptoException(String message){
            super(message);
        }
    }

    private static String toHex(byte[] data) {
        final StringBuilder sb = new StringBuilder(data.length * 2);
        for(byte singleByte : data){
            sb.append(DIGITS[(singleByte >>> 4) & 0x0F]);
            sb.append(DIGITS[singleByte & 0x0F]);
        }
        return sb.toString();
    }

	public static String encrypt(String key, String value) throws AESCryptoException{
		try{
            SecureRandom random = new SecureRandom();
            byte[] randBytes = new byte[IVLENGTH];
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            random.nextBytes(randBytes);
            IvParameterSpec iv = new IvParameterSpec(randBytes);

            String key2use=toHex(md.digest(key.getBytes("UTF-8"))).substring(0, KEYLENGTH).toLowerCase();
            SecretKeySpec skeySpec;
            skeySpec = new SecretKeySpec(key2use.getBytes("UTF-8") , "AES");

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            byte[] staged = cipher.doFinal(value.getBytes());
            byte[] encrypted = new byte[staged.length+randBytes.length];
            System.arraycopy(randBytes, 0, encrypted, 0, randBytes.length);
            System.arraycopy(staged, 0, encrypted, randBytes.length, staged.length);
            return Base64.encodeToString(encrypted,Base64.DEFAULT);
		} catch (UnsupportedEncodingException | InvalidAlgorithmParameterException | InvalidKeyException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException ex) {
		    throw new AESCryptoException("Error encrypting text!!");
		}
	}

    public static String decrypt(String key, String value) {
    	try {
            byte[] ivBytes = new byte[IVLENGTH];
            byte[] staged;
            try{
                staged = Base64.decode(value,Base64.DEFAULT);
            }catch (Exception ex){
                return ex.toString();
            }

            byte[] encrypted = new byte[staged.length-IVLENGTH];
            MessageDigest md = MessageDigest.getInstance("SHA-256"); 
            String key2use=toHex(md.digest(key.getBytes("UTF-8"))).substring(0,KEYLENGTH).toLowerCase();
            System.arraycopy(staged, 0, ivBytes, 0, ivBytes.length);
            System.arraycopy(staged, ivBytes.length, encrypted, 0, staged.length-ivBytes.length);
            IvParameterSpec iv = new IvParameterSpec(ivBytes);
            SecretKeySpec skeySpec = new SecretKeySpec(key2use.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            byte[] original = cipher.doFinal(encrypted);

            return new String(original);
        } catch (UnsupportedEncodingException | InvalidAlgorithmParameterException | InvalidKeyException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException ex) {
            throw new AESCryptoException("Error decrypting text!!");
        }
    }

}
