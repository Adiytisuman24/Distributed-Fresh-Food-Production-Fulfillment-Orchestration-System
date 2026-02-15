# FLAGSHIP DISTRIBUTED FOOD SYSTEM PROJECT (WWGST-CORE)
## Distributed Fresh Food Production & Fulfillment Orchestration System

### Mission
Build a fault-tolerant, data-driven distributed system that orchestrates fresh food production, quality SLAs, and same-day fulfillment across multiple kitchens and stores — while minimizing waste and protecting customer experience under real-world failures.

---

## 🧠 THE CORE PROBLEM
Food production is not SaaS. Machines fail, people are late, network drops, demand spikes suddenly, and food expires. This system is built to keep operating under chaos, make trade-offs automatically, and favor long-term customer trust over short-term throughput.

---

## 🗺️ CORE ARCHITECTURE (MENTAL MODEL)
Think of the system as 3 planes:
1. **Control Plane**: Decisions (planning, SLA, throttling).
2. **Execution Plane**: Actual food prep.
3. **Observability Plane**: Truth, metrics, audit.

*They are decoupled on purpose.*

---

## 🗺️ DISTRIBUTED FOOD SYSTEM ARCHITECTURE (DIAGRAM)
```text
Customer / Store
        |
        v
[ API Gateway ]
        |
        v
[ Order Intake Service ]
        |
        |  (validated, idempotent order)
        v
[ Production Planning Service ]
        |
        |  (prep tasks)
        v
     [ SQS Queue ]
        |
        v
[ Kitchen Orchestrator ]
        |
        |  (prep execution)
        v
[ Kitchen Systems / Staff / Machines ]
        |
        v
[ Quality & Freshness SLA Engine ]
        |
        |  (events + predictions)
        v
[ Failure & Backpressure Controller ]
        |
        v
[ Order Intake / Planner throttling ]

Parallel to all of this:
All Services  --->  Observability & Audit Service
```

---

## � STEP-BY-STEP FLOW (NORMAL CASE)

### 1️⃣ Order Intake (ENTRY POINT)
Order hits API Gateway. Order Intake Service:
- Validates SKU + freshness constraints.
- Applies idempotency key.
- Checks basic SLA feasibility.
👉 **Key decision**: If SLA is already impossible → reject immediately. Fail fast > fail late with bad customer experience.

### 2️⃣ Production Planning (CONTROL PLANE)
Planner answers: Which kitchen? When to prepare? Can we meet freshness SLA safely?
Inputs: Kitchen capacity, workload, equipment health, historical prep times.
Output: Prep tasks with deadlines. Tasks are pushed to SQS (not directly executed).
👉 **Why queue here?**: Decouples planning from execution, allows retries without duplication, and limits blast radius.

### 3️⃣ Kitchen Orchestrator (EXECUTION PLANE)
Responsibilities: Pull tasks from SQS, check staff/equipment availability, execute prep task, report progress.
- **Exactly-once guarantee**: Task state stored in DynamoDB.
- **Retries are safe**: No duplicate food prep.

### 4️⃣ Quality & Freshness SLA Engine (PREDICTIVE)
Tracks prep time, hold time, and transport window. It does prediction, not just monitoring:
“This order will miss freshness SLA in 7 minutes.”
👉 **Senior-level thinking**: Prevent, don’t react.

### 5️⃣ Failure & Backpressure Controller (SYSTEM PROTECTOR)
Your circuit breaker for reality. Reacts to SLA risk spikes, kitchen overload, queue growth, and equipment failures.
Actions: Throttle new orders, reject low-priority SKUs, pause certain kitchens.

### 6️⃣ Observability & Audit (TRUTH LAYER)
Every step emits metrics (P50/P99), events, and audit logs.
Answers: Who made this food? Where? When? Why was it delayed? Non-negotiable for food systems.

---

## 💥 FAILURE PATHS
❌ **Kitchen Goes Offline**: Orchestrator stops pulling tasks, Planner reassigns future tasks, Backpressure controller throttles intake. No cascading failure.

❌ **Demand Spike (Same-Day Rush)**: Queue depth rises, SLA engine predicts risk, Backpressure triggers. Result: Fewer orders, higher quality, lower waste.

❌ **Network Partition**: Idempotency prevents duplication, tasks retry safely, state reconciles on recovery. No human panic.

---

## 📊 WHY THIS ARCHITECTURE IS STRONG
| Design Choice | Why It Matters |
| :--- | :--- |
| **Event-driven** | Limits blast radius |
| **Idempotency everywhere** | Safe retries |
| **Predictive SLA** | Fewer fire drills |
| **Backpressure** | System survives spikes |
| **Audit-first** | Regulatory ready |

---

## � FILE STRUCTURE
```text
wcgst/
├── services/
│   ├── order-intake/           # API Entry & Validation
│   ├── production-planner/      # Task Decomposition & Rerouting
│   ├── kitchen-orchestrator/    # Task Execution & DynamoDB locking
│   ├── sla-engine/             # Predictive Freshness Monitoring
│   ├── failure-controller/     # Backpressure & Load Shedding
│   └── observability-audit/    # Traceability & Compliance Logs
├── dashboard/
│   ├── frontend/               # React/Vite Ops Panel
│   └── backend/                # Node.js Simulation Backend
├── infra/
│   ├── prometheus/             # Metrics Collection Config
│   └── grafana/                # Provisioned Dashboards
└── scripts/                    # Simulation & Load Generation
```

---

## 🎙️ SYSTEM DESIGN INTERVIEW NARRATION (SCRIPT)
“We separate decision-making from execution. Planning can change; execution must be safe. Failures are expected, so the system degrades intentionally instead of collapsing. We intentionally reject orders early when SLA risk is high because late food damages trust more than cancellation.”

---

## 🛠️ Step-by-Step Setup
1. **Infra**: `docker-compose up -d`
2. **Dashboard Backend**: `cd dashboard/backend && npm start`
3. **Dashboard Frontend**: `cd dashboard/frontend && npm run dev`
4. **Simulator**: `node scripts/simulate_orders.js`
