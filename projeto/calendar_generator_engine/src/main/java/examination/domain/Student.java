package examination.domain;

/**
 * @author Duarte
 * @version 1.0
 * @created 18-fev-2016 16:42:19
 */
public class Student {

	private long id;

	public Student(){

	}

	public void finalize() throws Throwable {

	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "Student id = " + id;
	}
}