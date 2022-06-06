package com.lti.data;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.lti.data.recast.functionmapping.model.CalculatedFormulaModel;
import com.lti.data.recast.functionmapping.services.CalculatedFormulaService;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RecastBoTableauFunctionMappingApplicationTests {
	CalculatedFormulaModel formula = new CalculatedFormulaModel();

	@Autowired
	CalculatedFormulaService service;

	/**
	 * Test for checking the function which is a invalid function name.
	 * 
	 * @throws IOException
	 */
	@Test
	public void invalidFunctionName() throws IOException {
		formula.setReportId("32456");
		formula.setFormula("=HELLO");
		formula.setCalculatedFormula("=INVALID FUNCTION NAME");
		formula.setColumnQualification("Unnamed Dimension");
		formula.setReportName("Recast_UseCase06");
		formula.setReportTabId("5");

		List<CalculatedFormulaModel> request = new ArrayList<CalculatedFormulaModel>();
		request.add(formula);
		List<CalculatedFormulaModel> response = new ArrayList<CalculatedFormulaModel>();

		response = service.boToTableauConversion(formula, response);
		for (CalculatedFormulaModel resp : response) {

			assertEquals(formula.getCalculatedFormula(), resp.getCalculatedFormula());

		}

	}

	/**
	 * Test for checking the function which is properly converted into tableau
	 * function or not.
	 * 
	 * @throws IOException
	 */

	public void convertedTableau() throws IOException {

		formula.setFormula("=AVERAGE([SALES])");
		formula.setCalculatedFormula("=AVG([SALES])");

		List<CalculatedFormulaModel> request = new ArrayList<CalculatedFormulaModel>();
		request.add(formula);
		List<CalculatedFormulaModel> response = new ArrayList<CalculatedFormulaModel>();

		response = service.boToTableauConversion(formula, response);
		for (CalculatedFormulaModel resp : response) {
			assertEquals(formula.getCalculatedFormula(), resp.getCalculatedFormula()); 
		}
	}

	/**
	 * Test for checking the function having null values.
	 * 
	 * @throws IOException
	 */

	public void nullChecking() throws IOException {

		formula.setFormula(null);
		formula.setCalculatedFormula(null);

		List<CalculatedFormulaModel> request = new ArrayList<CalculatedFormulaModel>();
		request.add(formula);
		List<CalculatedFormulaModel> response = new ArrayList<CalculatedFormulaModel>();

		response = service.boToTableauConversion(formula, response);
		for (CalculatedFormulaModel resp : response) {
			assertEquals(formula.getCalculatedFormula(), resp.getCalculatedFormula()); 
		}
	}

	/**
	 * Test for checking the function which is not available in tableau.
	 * 
	 * @throws IOException
	 */
	public void notAvailable() throws IOException {

		formula.setFormula("AVG([SALES])");
		formula.setCalculatedFormula("FUNCTION IS NOT AVAILABLE IN THE CURRENT TABLEAU VERSION");

		List<CalculatedFormulaModel> request = new ArrayList<CalculatedFormulaModel>();
		request.add(formula);
		List<CalculatedFormulaModel> response = new ArrayList<CalculatedFormulaModel>();

		response = service.boToTableauConversion(formula, response);
		for (CalculatedFormulaModel resp : response) {
			assertEquals(formula.getCalculatedFormula(), resp.getCalculatedFormula()); 
		}
	}
}