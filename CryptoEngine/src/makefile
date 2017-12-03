CC = gcc

IDIR = ../include
ODIR = obj
SHROBJ = /usr/local/lib/libtc.so

CFLAGS = -I$(IDIR)
_DEPS = test.h mathutils.h tc_internal.h
DEPS = $(patsubst %,$(IDIR)/%,$(_DEPS))

_OBJ = test.o
OBJ = $(patsubst %,$(ODIR)/%,$(_OBJ))

SRC=$(wildcard *.c)
 
#$(ODIR)/%.o: %.c $(DEPS)
#	$(CC) -o $@ $< $(SHROBJ) $(CFLAGS)
	
#test: $(OBJ)
#	$(CC) -o $@ $^ $(SHROBJ) $(CFLAGS)

$(ODIR)/test: $(SRC)
	$(CC) -o $@ $^ $(SHROBJ) $(CFLAGS)
	
.PHONEY: clean

clean:
	rm -f $(ODIR)/* *~ core $(INCDIR)/*~
