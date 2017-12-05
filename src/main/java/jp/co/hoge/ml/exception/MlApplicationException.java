package jp.co.hoge.ml.exception;

/**
 * @author Hiroki Ono
 */
public class MlApplicationException extends RuntimeException {

	private static final long serialVersionUID = 7637312312290605707L;

	public MlApplicationException() {}

	/**
	 * @param message
	 */
	public MlApplicationException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public MlApplicationException(Throwable cause) {
		this(cause.getMessage(), cause);
	}

	/**
	 * @param cause
	 * @param message
	 */
	public MlApplicationException(String message, Throwable cause) {
		super(message, cause);
	}

}
