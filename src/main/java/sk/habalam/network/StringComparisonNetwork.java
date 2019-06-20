package sk.habalam.network;

import java.io.IOException;

import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.nn.conf.BackpropType;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.SplitTestAndTrain;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerStandardize;
import org.nd4j.linalg.io.ClassPathResource;
import org.nd4j.linalg.learning.config.Sgd;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringComparisonNetwork {

	private static final Logger logger = LoggerFactory.getLogger(StringComparisonNetwork.class);

	public static void main(String[] args) throws IOException, InterruptedException {
		RecordReader recordReader = new CSVRecordReader();
		recordReader.initialize(new FileSplit(new ClassPathResource("/input/small_string_examples.csv").getFile()));

		int labelIndex = 200;
		int numbClasses = 2;
		int batchSize = 400;

		DataSetIterator iterator = new RecordReaderDataSetIterator(recordReader, batchSize, labelIndex, numbClasses);
		DataSet dataSet = iterator.next();
		dataSet.shuffle(42);

		DataNormalization normalizer = new NormalizerStandardize();
		normalizer.fit(dataSet);
		normalizer.transform(dataSet);

		SplitTestAndTrain testAndTrain = dataSet.splitTestAndTrain(0.5);
		DataSet trainingData = testAndTrain.getTrain();
		DataSet testingData = testAndTrain.getTest();

		long seed = 6;
		logger.info("Build model....");
		MultiLayerConfiguration configuration = new NeuralNetConfiguration.Builder()
			.seed(seed)
			.activation(Activation.TANH)
			.weightInit(WeightInit.XAVIER)
			.updater(new Sgd(0.5))
			.l2(1e-4)
			.list()
			.layer(new DenseLayer.Builder().nIn(200).nOut(150).build())
			.layer(new DenseLayer.Builder().nIn(150).nOut(100).build())
			.layer(new DenseLayer.Builder().nIn(100).nOut(50).build())
			.layer(new DenseLayer.Builder().nIn(50).nOut(25).build())
			.layer(new DenseLayer.Builder().nIn(25).nOut(12).build())
			.layer(new DenseLayer.Builder().nIn(12).nOut(6).build())
			.layer(new DenseLayer.Builder().nIn(6).nOut(3).build())
			.layer(new DenseLayer.Builder().nIn(3).nOut(2).build())
			.layer(new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD).activation(Activation.SOFTMAX).nIn(2).nOut(2).build())
			.backpropType(BackpropType.Standard)
			.build();

		MultiLayerNetwork network = new MultiLayerNetwork(configuration);
		network.init();
		network.setListeners(new ScoreIterationListener(100));

		for (int i = 0; i < 10000; i++) {
			network.fit(trainingData);
		}

		Evaluation eval = new Evaluation(2);
		INDArray output = network.output(testingData.getFeatures());
		eval.eval(testingData.getLabels(), output);
		logger.info(eval.stats());
	}
}
