package com.lti.data.recast.functionmapping.repository;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.lti.data.recast.functionmapping.model.FunctionModel;

public interface FunctionRepository extends JpaRepository<FunctionModel, Integer> {
	/**
	 * Query to get corresponding tableau function for sapbo function
	 * @return
	 */
	@Query(value = "SELECT tableau_Function FROM sapbo_tableau_function_mapping", nativeQuery = true)
	public ArrayList<String> findAlltableau_Function();

}
