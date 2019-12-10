package com.example.demo;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.example.demo.customerfile.CustomerFile;
import com.example.demo.customerfile.CustomerFileController.MessageWrapper;
import com.example.demo.message.Channel;
import com.example.demo.message.Message;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.extern.java.Log;


/**
 * Test of the solution, focused on the test scenario (more integration test than unit test)
 * No real value to unit test directly Spring Data Rest (already tested by Spring team)
 * 
 * @author Cyril Gambis
 * @date 9 déc. 2019
 *
 */
//JUnit 5
@ExtendWith(SpringExtension.class)

@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Log
class DemoApplicationTests {

	private static final String CUSTOMER_FILES_ENDPOINT = "/customerFiles";
	private static final String CUSTOMER_FILES_CUSTOM_ENDPOINT = "/customerFiles-custom";
	private static final String MESSAGES_ENDPOINT = "/messages";
	
	private static final String CUSTOMER_FILES_FIND_BY_REF_ENDPOINT = "/search/findByRef";
	
	@Autowired
	private MockMvc mockMvc;

	private static ObjectMapper objectMapper;
	
	/**
	 * Before all: initial configuration of the tests. Done once only.
	 *
	 * @author Cyril Gambis
	 * @date 9 déc. 2019
	 */
	@BeforeAll
    public static void setup() {
		objectMapper = new ObjectMapper().disable(MapperFeature.USE_ANNOTATIONS);

		// Manage dates as ISO strings
		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		// Do not put null fields in serialization
		objectMapper.setSerializationInclusion(Include.NON_NULL);
		
		log.info("Setup completed");
    }
	
	@Test
	void contextLoads() {
	}

	@Test
	public void whenGetCustomerFiles_thenStatusIsOk() throws Exception {
		
		mockMvc.perform(get(CUSTOMER_FILES_ENDPOINT))
		.andDo(print())
		.andExpect(status().isOk());

	}
	
	@Test
	public void givingValidMessage_whenPostToMessages_thenStatusIsCreatedAndLocationHeaderIsNotNull() throws Exception {
		
		Message message = new Message();
		message.setAuthor("Jérémie Durand");

		
		mockMvc.perform(post(MESSAGES_ENDPOINT)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(message))
				)
		.andDo(print())
		.andExpect(status().isCreated())
		.andExpect(header().string("Location", containsString("/messages")))
		;

	}
 
	
	/**
	 * Test of the standard scenario
	 *
	 * @author Cyril Gambis
	 * @date 9 déc. 2019
	 */
	@Test
	public void givingFullScenario_thenStatusIsOk() throws Exception {
		
		String customerFileRef = "KA-18B6-2";
		
		Message message = new Message();
		message.setAuthor("Jérémie Durand");
		message.setContent("Bonjour, j’ai un problème avec mon nouveau téléphone");
		message.setChannel(Channel.TWITTER);

		ResultActions resultMessage = mockMvc.perform(post(MESSAGES_ENDPOINT)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(message))
				)
		.andDo(print())
		.andExpect(status().isCreated())
		.andExpect(header().string(HttpHeaders.LOCATION, containsString(MESSAGES_ENDPOINT)))
		;

		String locationOfMessage = resultMessage.andReturn().getResponse().getHeader(HttpHeaders.LOCATION);
		
		
		CustomerFile customerFile = new CustomerFile();
		customerFile.setCustomer("Jérémie Durand");

		ResultActions resultCustomerFile = mockMvc.perform(post(CUSTOMER_FILES_ENDPOINT)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(customerFile))
				)
		.andDo(print())
		.andExpect(status().isCreated())
		.andExpect(header().string(HttpHeaders.LOCATION, containsString(CUSTOMER_FILES_ENDPOINT)))
		;
		
		String locationOfCustomerFile = resultCustomerFile.andReturn().getResponse().getHeader(HttpHeaders.LOCATION);
		
		
		addMessageToCustomerFile(locationOfMessage, locationOfCustomerFile);
		
		
		
		message = new Message();
		message.setAuthor("Sonia Valentin");
		message.setContent("Je suis Sonia, et je vais mettre tout en oeuvre pour vous aider. Quel est le modèle de votre téléphone ?");
		message.setChannel(Channel.TWITTER);

		resultMessage = mockMvc.perform(post(MESSAGES_ENDPOINT)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(message))
				)
		.andDo(print())
		.andExpect(status().isCreated())
		.andExpect(header().string(HttpHeaders.LOCATION, containsString(MESSAGES_ENDPOINT)))
		;

		locationOfMessage = resultMessage.andReturn().getResponse().getHeader(HttpHeaders.LOCATION);
		
		addMessageToCustomerFile(locationOfMessage, locationOfCustomerFile);
		
		customerFile = new CustomerFile();
		customerFile.setRef(customerFileRef);
		customerFile.setOpeningDateTime(null);
		
		mockMvc.perform(patch(locationOfCustomerFile)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(customerFile))
				)
		.andDo(print())
		.andExpect(status().isNoContent())
		;
		
		
		mockMvc.perform(get(CUSTOMER_FILES_ENDPOINT))
		.andDo(print())
		.andExpect(status().isOk())
		;
		
		mockMvc.perform(get(CUSTOMER_FILES_ENDPOINT + CUSTOMER_FILES_FIND_BY_REF_ENDPOINT + "?ref=" + customerFileRef))
		.andDo(print())
		.andExpect(status().isOk())
		.andExpect(jsonPath("$._embedded.customerFiles[0].ref", is(customerFileRef)))
		.andExpect(jsonPath("$._embedded.customerFiles[0].customer").value("Jérémie Durand"))
		;
		
		
		mockMvc.perform(get(locationOfCustomerFile + MESSAGES_ENDPOINT))
		.andDo(print())
		.andExpect(status().isOk())
		.andExpect(jsonPath("$._embedded.messages.length()").value(2))
		;

	}
	
	private void addMessageToCustomerFile(String locationOfMessage, String locationOfCustomerFile) throws Exception {
		mockMvc.perform(post(locationOfCustomerFile + MESSAGES_ENDPOINT)
				.contentType("text/uri-list")
				.content(locationOfMessage)
				)
		.andDo(print())
		.andExpect(status().isNoContent())
		;
	}
	

	/**
	 * Other solution, not always relying on Hateoas (extracting ids from uris)
	 *
	 * @author Cyril Gambis
	 * @date 9 déc. 2019
	 */
	@Test
	public void givingFullScenario_whenNotHateoas_thenStatusIsOk() throws Exception {
		
		String customerFileRef = "KA-18B6-2";
		
		Message message = new Message();
		message.setAuthor("Jérémie Durand");
		message.setContent("Bonjour, j’ai un problème avec mon nouveau téléphone");
		message.setChannel(Channel.TWITTER);

		ResultActions resultMessage = mockMvc.perform(post(MESSAGES_ENDPOINT)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(message))
				)
		.andDo(print())
		.andExpect(status().isCreated())
		.andExpect(header().string(HttpHeaders.LOCATION, containsString(MESSAGES_ENDPOINT)))
		;

		String locationOfMessage = resultMessage.andReturn().getResponse().getHeader(HttpHeaders.LOCATION);
		
		
		CustomerFile customerFile = new CustomerFile();
		customerFile.setCustomer("Jérémie Durand");

		ResultActions resultCustomerFile = mockMvc.perform(post(CUSTOMER_FILES_ENDPOINT)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(customerFile))
				)
		.andDo(print())
		.andExpect(status().isCreated())
		.andExpect(header().string(HttpHeaders.LOCATION, containsString(CUSTOMER_FILES_ENDPOINT)))
		;
		
		String locationOfCustomerFile = resultCustomerFile.andReturn().getResponse().getHeader(HttpHeaders.LOCATION);
		
		
		addMessageToCustomerFileNotHateoas(locationOfMessage, locationOfCustomerFile);
		
		message = new Message();
		message.setAuthor("Sonia Valentin");
		message.setContent("Je suis Sonia, et je vais mettre tout en oeuvre pour vous aider. Quel est le modèle de votre téléphone ?");
		message.setChannel(Channel.TWITTER);

		resultMessage = mockMvc.perform(post(MESSAGES_ENDPOINT)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(message))
				)
		.andDo(print())
		.andExpect(status().isCreated())
		.andExpect(header().string(HttpHeaders.LOCATION, containsString(MESSAGES_ENDPOINT)))
		;

		locationOfMessage = resultMessage.andReturn().getResponse().getHeader(HttpHeaders.LOCATION);
		
		addMessageToCustomerFileNotHateoas(locationOfMessage, locationOfCustomerFile);
		
		customerFile = new CustomerFile();
		customerFile.setRef(customerFileRef);
		customerFile.setOpeningDateTime(null);
		
		mockMvc.perform(patch(locationOfCustomerFile)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(customerFile))
				)
		.andDo(print())
		.andExpect(status().isNoContent())
		;
		
		
		mockMvc.perform(get(CUSTOMER_FILES_ENDPOINT))
		.andDo(print())
		.andExpect(status().isOk())
		;
		
		mockMvc.perform(get(CUSTOMER_FILES_ENDPOINT + CUSTOMER_FILES_FIND_BY_REF_ENDPOINT + "?ref=" + customerFileRef))
		.andDo(print())
		.andExpect(status().isOk())
		.andExpect(jsonPath("$._embedded.customerFiles[0].ref", is(customerFileRef)))
		.andExpect(jsonPath("$._embedded.customerFiles[0].customer").value("Jérémie Durand"))
		;
		
		
		mockMvc.perform(get(locationOfCustomerFile + MESSAGES_ENDPOINT))
		.andDo(print())
		.andExpect(status().isOk())
		.andExpect(jsonPath("$._embedded.messages.length()").value(2))
		;

	}
	
	
	
	/**
	 * Call to the custom method of the CustomerFilesController
	 *
	 * @author Cyril Gambis
	 * @date 9 déc. 2019
	 */
	private void addMessageToCustomerFileNotHateoas(String locationOfMessage, String locationOfCustomerFile) throws Exception {
		
		MessageWrapper messageWrapper = new MessageWrapper(getIdFromUri(locationOfMessage));
		Long customerFileId = getIdFromUri(locationOfCustomerFile);
		
		mockMvc.perform(post(CUSTOMER_FILES_CUSTOM_ENDPOINT + "/" + customerFileId + MESSAGES_ENDPOINT)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(objectMapper.writeValueAsString(messageWrapper))
				)
		.andDo(print())
		.andExpect(status().isOk())
		;
	}
	
	
	/**
	 * @param uri of a resource
	 * @return the "database" id of the resource, as a Long
	 * 
	 * IllegalArgumentException throwed if uri doesn't contain a Long at its end (or null, or not an uri)
	 *
	 * @author Cyril Gambis
	 * @date 9 déc. 2019
	 */
	private static final Long getIdFromUri(String uri) {
		if (uri == null || uri.length() < 2 || uri.indexOf('/') < 0) {
			throw new IllegalArgumentException(
					"No id on this uri: either null or too small or doesn't contain any slash");
		}
		String idAsString = uri.substring(uri.lastIndexOf('/') + 1);
		
		try {
			long id = Long.parseLong(idAsString);
			return id;
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("This uri doesn't contain any id. It must be a Long");
		}
	}
	
}
