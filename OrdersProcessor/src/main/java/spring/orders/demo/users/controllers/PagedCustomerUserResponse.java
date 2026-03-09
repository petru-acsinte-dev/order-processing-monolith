package spring.orders.demo.users.controllers;

import java.util.List;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import spring.orders.demo.users.dto.CustomerUserResponse;

public class PagedCustomerUserResponse {
	@ArraySchema(schema = @Schema(implementation = CustomerUserResponse.class))
	private List<CustomerUserResponse> content;

	private int totalPages;
	private long totalElements;
	private int number;
	private int size;
	private boolean first;
	private boolean last;

	public List<CustomerUserResponse> getContent() {
		return content;
	}
	public void setContent(List<CustomerUserResponse> content) {
		this.content = content;
	}
	public int getTotalPages() {
		return totalPages;
	}
	public void setTotalPages(int totalPages) {
		this.totalPages = totalPages;
	}
	public long getTotalElements() {
		return totalElements;
	}
	public void setTotalElements(long totalElements) {
		this.totalElements = totalElements;
	}
	public int getNumber() {
		return number;
	}
	public void setNumber(int number) {
		this.number = number;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	public boolean isFirst() {
		return first;
	}
	public void setFirst(boolean first) {
		this.first = first;
	}
	public boolean isLast() {
		return last;
	}
	public void setLast(boolean last) {
		this.last = last;
	}

}
