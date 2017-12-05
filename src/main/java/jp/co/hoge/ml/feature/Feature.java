package jp.co.hoge.ml.feature;

import jp.co.hoge.ml.util.vectorize.annotation.Csv;
import jp.co.hoge.ml.util.vectorize.annotation.DiscreteValue;
import jp.co.hoge.ml.util.vectorize.annotation.Range;

/**
 * @author Hiroki Ono
 */
public class Feature {

	@DiscreteValue
	private int osTypeId;

	private String userId;

	private String domain;

	@Csv
	private String categoryIds;

	@Range(maxValue = 1000)
	private int budget;

	private double empiricalCtr;

	public int getOsTypeId() {
		return osTypeId;
	}

	public void setOsTypeId(int osTypeId) {
		this.osTypeId = osTypeId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getCategoryIds() {
		return categoryIds;
	}

	public void setCategoryIds(String categoryIds) {
		this.categoryIds = categoryIds;
	}

	public int getBudget() {
		return budget;
	}

	public void setBudget(int budget) {
		this.budget = budget;
	}

	public double getEmpiricalCtr() {
		return empiricalCtr;
	}

	public void setEmpiricalCtr(double empiricalCtr) {
		this.empiricalCtr = empiricalCtr;
	}

}
