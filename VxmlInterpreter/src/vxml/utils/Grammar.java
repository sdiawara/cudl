package vxml.utils;

public class Grammar {
	public String root;
	public String mode;
	public String tagFormat;
	public String src;
	public String scope;
	public String type;
	public String weight;
	public String fetchhint;
	public String fetchtimeout;
	public String maxage;
	public String maxstale;

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Grammar) {
			Grammar g = (Grammar) obj;
			return root.endsWith(g.root) && fetchhint.endsWith(g.fetchhint)
					&& mode.endsWith(g.mode) && tagFormat.endsWith(g.tagFormat)
					&& src.endsWith(g.src) && scope.endsWith(g.scope)
					&& type.endsWith(g.type) && weight.endsWith(g.weight)
					&& fetchtimeout.endsWith(g.fetchtimeout)
					&& maxage.endsWith(g.maxage)
					&& maxstale.endsWith(g.maxstale);
		}
		return false;
	}

	@Override
	public String toString() {
		return super.toString();
	}
}
