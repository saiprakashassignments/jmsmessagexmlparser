package trng.jms.pojo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name="head")
public class Header {
	@Column
	private String para;
	@Id
	@Column
	private int id;
	@Column
	private String name;
	@Column
	private int age;

	public String getPara() {
		return para;
	}

	public void setPara(String para) {
		this.para = para;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}
}
