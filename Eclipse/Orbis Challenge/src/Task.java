public abstract class Task {
	enum Type {
		NestBuilder, FortressBuilder
	}

	abstract Type getType();
}
