package smartrics.rest.fitnesse.fixture.support;

import fit.Parse;

public class StatusCodeTypeAdapter extends RestDataTypeAdapter{

	@Override
	@SuppressWarnings("unchecked")
	public boolean equals(Object r1, Object r2) {
		if (r1 == null || r2 == null)
			return false;
		String expected = r1.toString();
		if(r1 instanceof Parse){
			expected = ((Parse)r1).text();
		}
		String actual = (String) r2;
		if (!Tools.regex(actual, expected)) {
			addError("not match: " + expected);
		}
		return getErrors().size() == 0;
	}

	@Override
	public Object parse(String s) {
		if(s==null)
			return "null";
		return s.trim();
	}

	@Override
	@SuppressWarnings("unchecked")
	public String toString(Object obj) {
		if(obj==null)
			return "null";
		if(obj.toString().trim().equals(""))
			return "blank";
		return obj.toString();
	}
}
