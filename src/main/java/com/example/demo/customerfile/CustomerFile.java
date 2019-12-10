package com.example.demo.customerfile;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import com.example.demo.message.Message;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Setter
@Getter
@ToString
@NoArgsConstructor
public class CustomerFile {

	@Id
	@GeneratedValue
	private Long id;
	
	private String customer;
	private LocalDateTime openingDateTime = LocalDateTime.now();;

	private String ref;
	
	@OneToMany
	private List<Message> messages;

	
}
