package com.lti.data.recast.functionmapping.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "sapbo_tableau_function_mapping")
public class FunctionModel {
	
	/**
	 * POJO class to get the functions from database 
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	@Column
	private String sapbo_Function;
	@Column
	private String tableau_Function;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getSapbo_Function() {
		return sapbo_Function;
	}

	public void setSapbo_Function(String sapbo_Function) {
		this.sapbo_Function = sapbo_Function;
	}

	public String getTableau_Function() {
		return tableau_Function;
	}

	public void setTableau_Function(String tableau_Function) {
		this.tableau_Function = tableau_Function;
	}

}
