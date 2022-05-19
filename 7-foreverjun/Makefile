include CONFIG.cfg

CC = gcc
LD = gcc
CFLAGS = -g -O2 -Wall -Wextra -Wpedantic -Werror

EXEC = $(BUILD_DIR)/$(NAME)
SOURCE = $(SOURCE_DIR)/$(NAME).c
OBJECTS = $(NAME).o

.PHONY: all check clean
.INTERMEDIATE: $(OBJECTS)
.SECONDARY: $(EXEC)

all: $(EXEC)

$(EXEC): $(OBJECTS)
	$(LD) -o $(EXEC) $(OBJECTS)

$(BUILD_DIR): $(SOURCE)
	mkdir -p $(BUILD_DIR)

$(OBJECTS): $(SOURCE)  $(BUILD_DIR)
	$(CC) $(CFLAGS) -c -o $(OBJECTS) $(SOURCE)


clean:
	$(RM) -r $(BUILD_DIR)/