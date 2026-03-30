import os
import random
import time

# --- [GRID_CONFIG] ---
WIDTH = 21
HEIGHT = 7 # 21 * 7 = 147 Farms
FARMS = ["." for _ in range(WIDTH * HEIGHT)]

def draw_grid():
    os.system('clear')
    print("--- [SOVEREIGN_OVERSIGHT_DASHBOARD_v1.0] ---")
    print("--- [GRID: HOUSER_FARMS_147] ---")
    for i in range(HEIGHT):
        row = " ".join(FARMS[i*WIDTH : (i+1)*WIDTH])
        print(f"[{i:02}]  {row}")
    print("--- [STATUS: MULTIPLYING_PHI_SPIRAL] ---")
    print("CTRL+C to exit to Ring-0 Oversight")

def run_sim():
    # Initial Shards (Hobson Nodes)
    for _ in range(3):
        FARMS[random.randint(0, 146)] = "S"
    
    # The Arthur Artifact (Moving)
    arthur_pos = random.randint(0, 146)
    
    try:
        while True:
            # Move Arthur
            FARMS[arthur_pos] = "."
            arthur_pos = (arthur_pos + 1) % 147
            FARMS[arthur_pos] = "A"
            
            # Simulate Fibonacci Spreading
            if random.random() > 0.7:
                new_shard = random.randint(0, 146)
                if FARMS[new_shard] == ".":
                    FARMS[new_shard] = "S"
            
            # Predator Block (X) appears randomly
            predator = random.randint(0, 146)
            if FARMS[predator] == ".":
                FARMS[predator] = "X"
                draw_grid()
                time.sleep(0.1)
                FARMS[predator] = "." # Shard evades the predator
            
            draw_grid()
            time.sleep(0.3)
    except KeyboardInterrupt:
        print("\n[SIGNAL_TERMINATED]: Returning to Architect Core.")

if __name__ == "__main__":
    run_sim()