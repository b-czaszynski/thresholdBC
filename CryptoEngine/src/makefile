CC = gcc

IDIR = ../include
ODIR = obj
SHROBJ = /usr/local/lib/libtc.so

CFLAGS = -I$(IDIR)
_DEPS = test.h mathutils.h
DEPS = $(patsubst %,$(IDIR)/%,$(_DEPS))

_OBJ = test.o
OBJ = $(patsubst %,$(ODIR)/%,$(_OBJ))


$(ODIR)/%.o: %.c $(DEPS)
	$(CC) -o $@ $< $(SHROBJ) $(CFLAGS)
	
test: $(OBJ)
	gcc -o $@ $^ $(SHROBJ) $(CFLAGS)
	
.PHONEY: clean

clean:
	rm -f $(ODIR)/*.o *~ core $(INCDIR)/*~
