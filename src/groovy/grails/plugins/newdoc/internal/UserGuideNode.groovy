package grails.plugins.newdoc.internal

class UserGuideNode {
    UserGuideNode parent
    List children = []

    String name
    String title
    String file
	
	@Override
	// Implement groovy.transform.ToString as simply as possible
	public String toString() {
		return "UserGuideNode(${name}, ${title}, ${file})"
	}
}