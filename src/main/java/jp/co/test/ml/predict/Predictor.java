package jp.co.test.ml.predict;

import jp.co.test.ml.feature.Feature;

/**
 * @author Hiroki Ono
 */
public interface Predictor {
	
	double predict(Feature future);

}
