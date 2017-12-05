package jp.co.hoge.ml.predict;

import jp.co.hoge.ml.feature.Feature;

/**
 * @author Hiroki Ono
 */
public interface Predictor {
	
	double predict(Feature future);

}
