package com.example.demo.customerfile;

import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.exception.BadArgumentException;
import com.example.demo.message.Message;
import com.example.demo.message.MessageRepository;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Alternative implementation of some API
 * 
 * @author Cyril Gambis
 * @date 10 déc. 2019
 *
 */
@RestController
@RequestMapping("/customerFiles-custom")
public class CustomerFileController {

	@Autowired
	private CustomerFileRepository customerFileRepository;

	@Autowired
	private MessageRepository messageRepository;
	
	/**
	 * Wrapper to a message id, used to accept JSON as RequestBody of an api endpoint
	 * 
	 * @author Cyril Gambis
	 * @date 10 déc. 2019
	 *
	 */
	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class MessageWrapper {
		private Long messageId;
	}
	
	/**
	 * Link a message to a customer file, using the id of the message and of the customer file
	 * If the customer file or the message doesn't exist, throw an exception that will return a "Bad request" 400 HTTP Code
	 *
	 * @author Cyril Gambis
	 * @date 10 déc. 2019
	 */
	@PostMapping(value = "/{customerFileId}/messages", consumes = MediaType.APPLICATION_JSON_VALUE)
	@Transactional
	public ResponseEntity<?> addMessage(@RequestBody MessageWrapper messageWrapper, @PathVariable Long customerFileId) {
		CustomerFile customerFile = customerFileRepository.findById(customerFileId).orElseThrow(BadArgumentException::new);
		
		
		Optional<Message> message = messageRepository.findById(messageWrapper.getMessageId());
		customerFile.getMessages().add(message.orElseThrow(BadArgumentException::new)); 
		customerFileRepository.save(customerFile);
		
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
}
