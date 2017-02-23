package gov.cms.qpp.conversion.parser;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Element;
import org.junit.Test;

import gov.cms.qpp.conversion.model.Node;
import gov.cms.qpp.conversion.xml.XmlUtils;

public class ACIProportionMeasureParserTest {

	@Test
	public void parseACIProportionMeasureAsNode() throws Exception {
		String xmlFragment = 
				"<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" + 
				"<entry xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" + 
				"	<organizer classCode=\"CLUSTER\" moodCode=\"EVN\">\n" + 
				"		<!-- Implied template Measure Reference templateId -->\n" + 
				"		<templateId root=\"2.16.840.1.113883.10.20.24.3.98\"/>\n" + 
				"		<!-- ACI Numerator Denominator Type Measure Reference and Results templateId -->\n" + 
				"		<templateId root=\"2.16.840.1.113883.10.20.27.3.28\" extension=\"2016-09-01\"/>\n" + 
				"		<id root=\"ac575aef-7062-4ea2-b723-df517cfa470a\"/>\n" + 
				"		<statusCode code=\"completed\"/>\n" + 
				"		<reference typeCode=\"REFR\">\n" + 
				"			<!-- Reference to a particular ACI measure's unique identifier. -->\n" + 
				"			<externalDocument classCode=\"DOC\" moodCode=\"EVN\">\n" + 
				"				<!-- This is a temporary root OID that indicates this is an ACI measure identifier -->\n" + 
				"				<!-- extension is the unique identifier for an ACI measure. \"ACI-PEA-1\" is for illustration only. -->\n" + 
				"				<id root=\"2.16.840.1.113883.3.7031\" extension=\"ACI-PEA-1\"/>\n" + 
				"				<!-- ACI measure title -->\n" + 
				"				<text>Patient Access</text>\n" + 
				"			</externalDocument>\n" + 
				"		</reference>\n" + 
				"		<component>\n" + 
				"			<observation classCode=\"OBS\" moodCode=\"EVN\">\n" + 
				"				<!-- Performance Rate templateId -->\n" + 
				"				<templateId root=\"2.16.840.1.113883.10.20.27.3.30\"\n" + 
				"					extension=\"2016-09-01\"/>\n" + 
				"				<code code=\"72510-1\" codeSystem=\"2.16.840.1.113883.6.1\" codeSystemName=\"LOINC\" displayName=\"Performance Rate\"/>\n" + 
				"				<statusCode code=\"completed\"/>\n" + 
				"				<value xsi:type=\"REAL\" value=\"0.750000\"/>\n" + 
				"			</observation>\n" + 
				"		</component>\n" + 
				"		<component>\n" + 
				"			<observation classCode=\"OBS\" moodCode=\"EVN\">\n" + 
				"				<!-- ACI Numerator Denominator Type Measure Numerator Data templateId -->\n" + 
				"				<templateId root=\"2.16.840.1.113883.10.20.27.3.31\"\n" + 
				"					extension=\"2016-09-01\"/>\n" + 
				"				<code code=\"ASSERTION\" codeSystem=\"2.16.840.1.113883.5.4\"\n" + 
				"					codeSystemName=\"ActCode\" displayName=\"Assertion\"/>\n" + 
				"				<statusCode code=\"completed\"/>\n" + 
				"				<value xsi:type=\"CD\" code=\"NUMER\"\n" + 
				"					codeSystem=\"2.16.840.1.113883.5.4\" codeSystemName=\"ActCode\"/>\n" + 
				"				<!-- Numerator Count-->\n" + 
				"				<entryRelationship typeCode=\"SUBJ\" inversionInd=\"true\">\n" + 
				"					<observation classCode=\"OBS\" moodCode=\"EVN\">\n" + 
				"						<templateId root=\"2.16.840.1.113883.10.20.27.3.3\"/>\n" + 
				"						<code code=\"MSRAGG\" codeSystem=\"2.16.840.1.113883.5.4\"\n" + 
				"							codeSystemName=\"ActCode\"\n" + 
				"							displayName=\"rate aggregation\"/>\n" + 
				"						<statusCode code=\"completed\"/>\n" + 
				"						<value xsi:type=\"INT\" value=\"600\"/>\n" + 
				"						<methodCode code=\"COUNT\"\n" + 
				"							codeSystem=\"2.16.840.1.113883.5.84\"\n" + 
				"							codeSystemName=\"ObservationMethod\"\n" + 
				"							displayName=\"Count\"/>\n" + 
				"					</observation>\n" + 
				"				</entryRelationship>\n" + 
				"			</observation>\n" + 
				"		</component>\n" + 
				"		<component>\n" + 
				"			<observation classCode=\"OBS\" moodCode=\"EVN\">\n" + 
				"				<!-- ACI Numerator Denominator Type Measure Denominator Data templateId -->\n" + 
				"				<templateId root=\"2.16.840.1.113883.10.20.27.3.32\"\n" + 
				"					extension=\"2016-09-01\"/>\n" + 
				"				<code code=\"ASSERTION\" codeSystem=\"2.16.840.1.113883.5.4\"\n" + 
				"					codeSystemName=\"ActCode\" displayName=\"Assertion\"/>\n" + 
				"				<statusCode code=\"completed\"/>\n" + 
				"				<value xsi:type=\"CD\" code=\"DENOM\"\n" + 
				"					codeSystem=\"2.16.840.1.113883.5.4\" codeSystemName=\"ActCode\"/>\n" + 
				"				<!-- Denominator Count-->\n" + 
				"				<entryRelationship typeCode=\"SUBJ\" inversionInd=\"true\">\n" + 
				"					<observation classCode=\"OBS\" moodCode=\"EVN\">\n" + 
				"						<templateId root=\"2.16.840.1.113883.10.20.27.3.3\"/>\n" + 
				"						<code code=\"MSRAGG\" codeSystem=\"2.16.840.1.113883.5.4\"\n" + 
				"							codeSystemName=\"ActCode\"\n" + 
				"							displayName=\"rate aggregation\"/>\n" + 
				"						<statusCode code=\"completed\"/>\n" + 
				"						<value xsi:type=\"INT\" value=\"800\"/>\n" + 
				"						<methodCode code=\"COUNT\"\n" + 
				"							codeSystem=\"2.16.840.1.113883.5.84\"\n" + 
				"							codeSystemName=\"ObservationMethod\"\n" + 
				"							displayName=\"Count\"/>\n" + 
				"					</observation>\n" + 
				"				</entryRelationship>\n" + 
				"			</observation>\n" + 
				"		</component>\n" + 
				"	</organizer>\n" + 
				"</entry>";
		
		Element dom =  XmlUtils.stringToDOM(xmlFragment);
		
		QppXmlInputParser parser = new QppXmlInputParser();
		parser.setDom(dom);

		Node victim = parser.parse();

		// This node is the place holder around the root node
		assertThat("returned node should not be null", victim, is(not(nullValue())));

		// For all parsers this should be either a value or child node
		assertThat("returned node should have one child node", victim.getChildNodes().size(), is(1));
		// This is the child node that is produced by the intended parser
		Node aciProportionMeasureNode = victim.getChildNodes().get(0);
		// Should have a aggregate count node 
		assertThat("returned node should have two child parser nodes", aciProportionMeasureNode.getChildNodes().size(), is(2));
		
		assertThat("measureId should be ACI-PEA-1",
				(String) aciProportionMeasureNode.get("measureId"), is("ACI-PEA-1"));
	
		List<String> testTemplateIds = new ArrayList<>();
		for (Node node : aciProportionMeasureNode.getChildNodes()) {
			testTemplateIds.add(node.getIdTemplate());
		}
		
		assertThat("Should have Numerator", testTemplateIds.contains("2.16.840.1.113883.10.20.27.3.31"), is(true));
		assertThat("Should have Denominator", testTemplateIds.contains("2.16.840.1.113883.10.20.27.3.32"), is(true));

	}

}