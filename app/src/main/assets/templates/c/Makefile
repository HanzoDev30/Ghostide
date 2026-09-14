CC = gcc
CFLAGS = -Wall -Wextra -O2
TARGET = {{PROJECT_NAME_LOWER}}
SRC = src/main.c

all: $(TARGET)

$(TARGET): $(SRC)
	$(CC) $(CFLAGS) -o $(TARGET) $(SRC)

clean:
	rm -f $(TARGET)

.PHONY: all clean
