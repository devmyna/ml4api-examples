package jp.co.test.ml.predict;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import org.deeplearning4j.nn.api.Layer;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.layers.BaseLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.params.DefaultParamInitializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import jp.co.test.ml.exception.MlApplicationException;
import jp.co.test.ml.feature.Feature;
import jp.co.test.ml.util.vectorize.IndexMap;
import jp.co.test.ml.util.vectorize.VectorUtil;

/**
 * @author Hiroki Ono
 */
public class DeepLearningPredictor implements Predictor {

	private Layer[] layers;
	private IndexMap indexMap;

	/**
	 * @param conf
	 * @param weight
	 * @param idMapper
	 */
	public DeepLearningPredictor(String conf, byte[] weight, IndexMap indexMap) {
		init(conf, weight);
		this.indexMap = indexMap;
	}

	@Override
	public double predict(Feature feature) {
		INDArray input = convertToFeatureVec(feature);

		INDArray currInput = input;
		for (int i = 0; i < layers.length; i++) {

			// get weight 
			INDArray b = layers[i].getParam(DefaultParamInitializer.BIAS_KEY);
			INDArray w = layers[i].getParam(DefaultParamInitializer.WEIGHT_KEY);

			// apply weight
			INDArray z = currInput.mmul(w).addiRowVector(b);

			// apply activation function
			@SuppressWarnings("rawtypes")
			INDArray ret = ((BaseLayer) layers[i]).layerConf().getActivationFn().getActivation(z, false);

			currInput = ret;
		}

		return currInput.getDouble(0);
	}

	/**
	 * @param conf
	 * @param weight
	 */
	private void init(String conf, byte[] weight) {
		MultiLayerConfiguration confFromJson = MultiLayerConfiguration
				.fromJson(conf);
		MultiLayerNetwork model = new MultiLayerNetwork(confFromJson);
		model.init();

		try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(weight))) {
			INDArray newParams = Nd4j.read(dis);
			model.setParams(newParams);

		} catch (IOException e) {
			throw new MlApplicationException(e);
		}

		layers = model.getLayers();
	}

	/**
	 * @param future
	 * @return
	 */
	private INDArray convertToFeatureVec(Feature feature) {
		return Nd4j.create(VectorUtil.toVector(feature, indexMap));
	}

}
