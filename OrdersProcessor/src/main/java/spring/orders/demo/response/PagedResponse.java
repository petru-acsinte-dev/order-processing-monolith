package spring.orders.demo.response;

import java.util.List;
import java.util.function.Function;

import org.springframework.data.domain.Page;

import io.swagger.v3.oas.annotations.media.ArraySchema;

public class PagedResponse<T> {
	@ArraySchema
	private List<T> content;

	private int totalPages;
	private long totalElements;
	private int number;
	private int size;
	private boolean first;
	private boolean last;

	public static <T, R> PagedResponse<R> from(Page<T> page, Function<T, R> mapper) {
        final PagedResponse<R> response = new PagedResponse<>();
        response.content = page.getContent().stream().map(mapper).toList();
        response.totalPages = page.getTotalPages();
        response.totalElements = page.getTotalElements();
        response.number = page.getNumber();
        response.size = page.getSize();
        response.first = page.isFirst();
        response.last = page.isLast();
        return response;
    }

	public List<T> getContent() {
		return content;
	}
	public void setContent(List<T> content) {
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
