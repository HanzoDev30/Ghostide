LIB = lib{{PROJECT_NAME_LOWER}}.so

all: $(LIB)

$(LIB): lib.c lib.h
	gcc -shared -fPIC -o $(LIB) lib.c

clean:
	rm -f $(LIB)
