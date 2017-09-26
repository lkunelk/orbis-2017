public abstract class Task {
	enum Type {
		NestBuilder, FortressBuilder, NestBreaker
	}

	abstract Type getType();
}
