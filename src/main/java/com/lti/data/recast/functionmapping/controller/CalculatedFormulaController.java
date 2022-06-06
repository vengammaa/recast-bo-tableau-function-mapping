package com.lti.data.recast.functionmapping.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.lti.data.recast.functionmapping.model.CalculatedFormulaModel;
import com.lti.data.recast.functionmapping.services.CalculatedFormulaService;
//import com.lti.data.recast.functionmapping.validator.FormulaValidator;

@RestController
@Validated
@RequestMapping("/v1.0")

public class CalculatedFormulaController {

	@Autowired
	CalculatedFormulaService services;
	

	

	/**
	 * Controller function for sapBo to Tableau for list of calculated formula
	 * model. Request [reportId=32520, reportName=Recast_UseCase06, reportTabId=5,
	 * formula==count("test4"), calculatedFormula=, columnQualification=Unnamed
	 * Dimension] Response [reportId=32520, reportName=Recast_UseCase06,
	 * reportTabId=5, formula==count("test4"), calculatedFormula==COUNT("test4"),
	 * columnQualification=Unnamed Dimension]
	 * 
	 * @param sapBoFunction
	 * @return
	 * @throws Throwable
	 * @throws IOException/
	 */
	@RequestMapping(value = "/functionmapping", method = RequestMethod.POST, produces = "application/json")

	public ResponseEntity<List<CalculatedFormulaModel>> functionConversionResponse(
			@RequestBody List<@Valid CalculatedFormulaModel> sapBoFunction) throws IOException {
		List<CalculatedFormulaModel> response = new ArrayList<CalculatedFormulaModel>();
		for (CalculatedFormulaModel request : sapBoFunction) {
			
			response = services.boToTableauConversion(request,response);
			

		}
		return new ResponseEntity<List<CalculatedFormulaModel>>(response, HttpStatus.OK);
	}

}
