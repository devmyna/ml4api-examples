package jp.co.hoge.ml;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import jp.co.hoge.ml.feature.Feature;
import jp.co.hoge.ml.predict.DeepLearningPredictor;
import jp.co.hoge.ml.util.vectorize.IndexMap;
import jp.co.hoge.ml.util.vectorize.VectorUtil;

/**
 * @author Hiroki Ono
 */
public class App {

	public static void main(String[] args) {

		String confFile = "/tmp/conf.json";
		String binFile = "/tmp/weight.bin";

		List<Feature> features = createSampleFeatures();

		System.out.println("Create feature index map");
		IndexMap indexMap = VectorUtil.createIndexMap(features);

		MultiLayerConfiguration conf = getSampleModelConf(indexMap.size());

		// create model
		System.out.println("Train model");
		MultiLayerNetwork model = train(conf, features, indexMap, 0.1, 0.2);

		System.out.println("Save model");
		try (OutputStream fos = new FileOutputStream(binFile); DataOutputStream dos = new DataOutputStream(fos)) {
			Nd4j.write(model.params(), dos);
			FileUtils.writeStringToFile(new File(confFile), model.getLayerWiseConfigurations().toJson(), "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		System.out.println("Load model");
		String confString = null;
		byte[] weight = null;
		try {
			confString = FileUtils.readFileToString(new File(confFile), "UTF-8");
			weight = IOUtils.toByteArray(new FileInputStream(new File(binFile)));
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		System.out.println("Create thread safe predictor instance");
		DeepLearningPredictor dlPredictor = new DeepLearningPredictor(confString, weight, indexMap);
		
		System.out.println("Predict");
		features.parallelStream().forEach(feature ->{
			double result = dlPredictor.predict(feature);
			System.out.println(String.format("predict ctr : %f", result));
		});

	}

	/**
	 * @return {@link List<Feature>}
	 */
	private static List<Feature> createSampleFeatures() {
		List<Feature> features = new ArrayList<>();

		Feature f1 = new Feature();
		f1.setOsTypeId(1);
		f1.setUserId("userA");
		f1.setDomain("test.co.jp");
		f1.setCategoryIds("1,2,3");
		f1.setBudget(500);
		f1.setEmpiricalCtr(0.8);
		features.add(f1);

		Feature f2 = new Feature();
		f1.setOsTypeId(2);
		f1.setUserId("userB");
		f1.setDomain("test.co.jp");
		f1.setCategoryIds("1,3");
		f1.setBudget(1000);
		f1.setEmpiricalCtr(0.3);
		features.add(f2);

		return features;
	}

	/**
	 * @param numInputs
	 * @return {@link MultiLayerConfiguration}
	 */
	private static MultiLayerConfiguration getSampleModelConf(int numInputs) {

		int seed = 123;
		double learningRate = 0.01;
		int numOutputs = 1;
		int numHiddenNodes = 20;

		return new NeuralNetConfiguration.Builder().seed(seed).iterations(1)
				.optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT).learningRate(learningRate)
				.weightInit(WeightInit.XAVIER).updater(new Nesterovs(0.9)).list()
				.layer(0,
						new DenseLayer.Builder().nIn(numInputs).nOut(numHiddenNodes).activation(Activation.TANH)
								.build())
				.layer(1,
						new DenseLayer.Builder().nIn(numHiddenNodes).nOut(numHiddenNodes).activation(Activation.TANH)
								.build())
				.layer(2, new OutputLayer.Builder(LossFunctions.LossFunction.MSE).activation(Activation.IDENTITY)
						.nIn(numHiddenNodes).nOut(numOutputs).build())
				.pretrain(false).backprop(true).build();

	}

	/**
	 * @param model
	 * @param features
	 * @param indexMap
	 * @param outputValues
	 */
	private static MultiLayerNetwork train(MultiLayerConfiguration conf, List<Feature> features, IndexMap indexMap,
			double... outputValues) {

		MultiLayerNetwork model = new MultiLayerNetwork(conf);
		model.init();

		int dateSetNum = features.size();
		for (int i = 0; i < dateSetNum; i++) {
			INDArray input = Nd4j.create(VectorUtil.toVector(features.get(0), indexMap));
			INDArray output = Nd4j.create(new double[] { outputValues[i] });
			model.fit(new DataSet(input, output));
		}

		return model;
	}
}
