import ctypes
import os

# Load the compiled C library
_lib_path = os.path.join(os.path.dirname(__file__), "lib{{PROJECT_NAME_LOWER}}.so")
_lib = ctypes.CDLL(_lib_path)

# Define function signatures
_lib.add.argtypes = [ctypes.c_int, ctypes.c_int]
_lib.add.restype = ctypes.c_int

def add(a: int, b: int) -> int:
    return _lib.add(a, b)

def main():
    result = add(3, 4)
    print(f"Hello from {{PROJECT_NAME}}! 3 + 4 = {result}")

if __name__ == "__main__":
    main()
