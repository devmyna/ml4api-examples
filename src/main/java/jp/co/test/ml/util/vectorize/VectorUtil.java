package jp.co.test.ml.util.vectorize;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.nd4j.linalg.io.CollectionUtils;

import jp.co.test.ml.exception.MlApplicationException;
import jp.co.test.ml.util.vectorize.annotation.Csv;
import jp.co.test.ml.util.vectorize.annotation.DiscreteValue;
import jp.co.test.ml.util.vectorize.annotation.Range;

/**
 * @author Hiroki Ono
 */
public class VectorUtil {

	/**
	 * @param dataSet
	 * @return
	 */
	public static <E> IndexMap createIndexMap(Collection<E> dataSet) {
		return appendIndexMap(dataSet, null);
	}

	/**
	 * @param dataSet
	 * @param map
	 * @return
	 */
	public static <E> IndexMap appendIndexMap(Collection<E> dataSet, IndexMap map) {

		IndexMap indexMap = null;
		if (map != null) {
			indexMap = map;
		} else {
			indexMap = new IndexMap();
		}

		if (CollectionUtils.isEmpty(dataSet)) {
			return indexMap;
		}

		// init field list
		List<Field> lis = null;
		for (E e : dataSet) {
			lis = Stream.of(e.getClass().getDeclaredFields()).sorted(Comparator.comparing(Field::getName))
					.collect(Collectors.toList());
			break;
		}

		int maxIndex = 0;
		try {
			for (E e : dataSet) {

				for (Field field : lis) {

					Class<?> type = field.getType();
					String fieldName = field.getName();
					field.setAccessible(true);
					Object obj = field.get(e);
					if (obj == null) {
						continue;
					}

					if (type.equals(String.class)) {
						String val = (String) field.get(e);
						Annotation annotation = field.getAnnotation(Csv.class);
						if (annotation != null) {
							String[] valArray = val.split(((Csv) annotation).delimiter());
							for (String content : valArray) {
								if (!indexMap.containsKey(fieldName.concat(content).hashCode())) {
									indexMap.putFeatureIndex(fieldName.concat(content).hashCode(), maxIndex++);
								}
							}
						} else {
							if (!indexMap.containsKey(fieldName.concat(val).hashCode())) {
								indexMap.putFeatureIndex(fieldName.concat(val).hashCode(), maxIndex++);
							}
						}

					} else if (type.equals(int.class) || type.equals(double.class)) {

						Annotation annotation = field.getAnnotation(DiscreteValue.class);
						if (annotation != null) {
							if (!indexMap.containsKey(fieldName.concat(String.valueOf(obj)).hashCode())) {
								indexMap.putFeatureIndex(fieldName.concat(String.valueOf(obj)).hashCode(), maxIndex++);
							}

						} else {
							if (!indexMap.containsKey(fieldName.hashCode())) {
								indexMap.putFeatureIndex(fieldName.hashCode(), maxIndex++);
							}
						}
					} else {
						throw new MlApplicationException(String.format(
								"Expected field type : [int.class, double.class, String.class], intput type : [%s]",
								type));
					}
				}
			}
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new MlApplicationException(e);
		}

		return indexMap;
	}

	/**
	 * @param feature
	 * @param indexMap
	 * @return
	 */
	public static <T> double[] toVector(T feature, IndexMap indexMap) {

		if (feature == null || indexMap == null) {
			throw new NullPointerException("feature or indexMap is null.");
		}

		double[] vec = new double[indexMap.size()];
		List<Field> lis = Stream.of(feature.getClass().getDeclaredFields()).sorted(Comparator.comparing(Field::getName))
				.collect(Collectors.toList());

		for (Field field : lis) {
			try {

				Class<?> type = field.getType();
				String fieldName = field.getName();
				field.setAccessible(true);
				Object obj = field.get(feature);
				if (obj == null) {
					continue;
				}

				if (type.equals(String.class)) {
					String val = (String) field.get(feature);
					Annotation annotation = field.getAnnotation(Csv.class);
					if (annotation != null) {
						String[] valArray = val.split(((Csv) annotation).delimiter());
						for (String content : valArray) {
							int index = indexMap.getFeatureIndex(fieldName.concat(content).hashCode());
							if (index != -1) {
								vec[index] = 1;
							}
						}
					} else {
						int index = indexMap.getFeatureIndex(fieldName.concat(val).hashCode());
						if (index != -1) {
							vec[index] = 1;
						}
					}

				} else if (type.equals(int.class) || type.equals(double.class)) {
					Annotation annotation = null;
					if ((annotation = field.getAnnotation(DiscreteValue.class)) != null) {
						int index = indexMap.getFeatureIndex(fieldName.concat(String.valueOf(obj)).hashCode());
						if (index != -1) {
							vec[index] = 1;
						}
					} else if ((annotation = field.getAnnotation(Range.class)) != null) {
						double valRaw = ((Number) obj).doubleValue();
						double val = (valRaw / ((Range) annotation).maxValue());
						int index = indexMap.getFeatureIndex(fieldName.hashCode());
						if (index != -1) {
							vec[index] = val;
						}
					} else {
						int index = indexMap.getFeatureIndex(fieldName.hashCode());
						if (index != -1) {
							vec[index] = (((Number) obj).doubleValue());
						}
					}

				} else {
					throw new MlApplicationException(String.format(
							"Expected field type : [int.class, double.class, String.class], input type : [%s]", type));
				}

			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new MlApplicationException(e);
			}
		}

		return vec;
	}

}
