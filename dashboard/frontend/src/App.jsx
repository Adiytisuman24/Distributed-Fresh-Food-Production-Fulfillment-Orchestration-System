import React, { useState, useEffect } from 'react';
import { 
  Activity, 
  AlertTriangle, 
  CheckCircle, 
  Clock, 
  Database, 
  Flame, 
  Layers, 
  Monitor, 
  Package, 
  Server, 
  ShieldCheck, 
  Zap 
} from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';
import { 
  AreaChart, 
  Area, 
  XAxis, 
  YAxis, 
  CartesianGrid, 
  Tooltip, 
  ResponsiveContainer 
} from 'recharts';
import io from 'socket.io-client';

const socket = io('http://localhost:5000');

const App = () => {
  const [logs, setLogs] = useState([]);
  const [metrics, setMetrics] = useState({
    throughput: 124,
    slaBreach: 1.2,
    waste: 0.8,
    activeTasks: 42
  });
  const [systemStatus, setSystemStatus] = useState('OPERATIONAL');

  useEffect(() => {
    socket.on('NEW_ORDER', (order) => {
      addLog(`NEW_ORDER: Order ${order.orderId} accepted from Store ${order.storeId}`);
    });

    socket.on('FAILURE_INJECTED', (failure) => {
      addLog(`CRITICAL: Failure injected - ${failure.type} in ${failure.kitchenId}`, 'error');
      setSystemStatus('DEGRADED');
    });

    return () => socket.off();
  }, []);

  const addLog = (message, type = 'info') => {
    setLogs(prev => [{ id: Date.now(), message, type, time: new Date().toLocaleTimeString() }, ...prev].slice(0, 10));
  };

  const injectFailure = (type) => {
    fetch('http://localhost:5000/api/simulate/failure', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ type, kitchenId: 'KITCHEN-A' })
    });
  };

  return (
    <div className="min-h-screen w-full p-8 font-['Inter']">
      {/* Header */}
      <header className="flex justify-between items-center mb-10">
        <div>
          <h1 className="text-3xl font-bold font-['Outfit'] premium-gradient-text">DFS | CORE</h1>
          <p className="text-gray-400 text-sm mt-1">Distributed Production & Fulfillment Orchestrator</p>
        </div>
        <div className={`flex items-center px-4 py-2 rounded-full glass-card ${systemStatus === 'OPERATIONAL' ? 'text-green-400' : 'text-yellow-400'}`}>
          <div className={`w-3 h-3 rounded-full mr-3 ${systemStatus === 'OPERATIONAL' ? 'bg-green-500 shadow-[0_0_10px_#22c55e]' : 'bg-yellow-500 status-pulse'} `}></div>
          <span className="text-sm font-semibold uppercase tracking-wider">{systemStatus}</span>
        </div>
      </header>

      {/* Metrics Row */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
        {[
          { icon: Package, label: 'Throughput', value: `${metrics.throughput} OPM`, color: 'text-blue-400' },
          { icon: AlertTriangle, label: 'SLA Breach Rate', value: `${metrics.slaBreach}%`, color: 'text-orange-400' },
          { icon: Flame, label: 'Waste %', value: `${metrics.waste}%`, color: 'text-red-400' },
          { icon: Activity, label: 'Active Tasks', value: metrics.activeTasks, color: 'text-purple-400' }
        ].map((stat, i) => (
          <motion.div 
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: i * 0.1 }}
            key={stat.label} 
            className="glass-card p-6"
          >
            <div className="flex items-center mb-4">
              <stat.icon className={`w-5 h-5 ${stat.color} mr-3`} />
              <span className="text-gray-400 text-sm font-medium">{stat.label}</span>
            </div>
            <div className="text-2xl font-bold">{stat.value}</div>
          </motion.div>
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Live Simulation Control */}
        <div className="lg:col-span-1 space-y-6">
          <div className="glass-card p-6 h-full">
            <h2 className="text-lg font-bold mb-6 flex items-center">
              <Zap className="w-5 h-5 text-yellow-400 mr-2" />
              Simulation Controller
            </h2>
            <div className="grid grid-cols-1 gap-4">
              <SimulationButton 
                label="Simulate Oven Failure" 
                subtext="Cuts Kitchen-A capacity by 50%" 
                variant="danger" 
                onClick={() => injectFailure('OVEN_BREAKDOWN')}
              />
              <SimulationButton 
                label="Demand Spike (100x)" 
                subtext="Triggers Backpressure Throttling" 
                variant="warning"
                onClick={() => injectFailure('DEMAND_SPIKE')}
              />
              <SimulationButton 
                label="Network Partition" 
                subtext="Force offline idempotency check" 
                variant="info"
                onClick={() => injectFailure('NETWORK_PARTITION')}
              />
              <SimulationButton 
                label="Staff Shortage" 
                subtext="Increases Mean Prep Time" 
                variant="info"
                onClick={() => injectFailure('STAFF_SHORTAGE')}
              />
            </div>
          </div>
        </div>

        {/* Live Terminal & System Log */}
        <div className="lg:col-span-2 space-y-6">
          <div className="glass-card p-6 min-h-[400px]">
            <h2 className="text-lg font-bold mb-6 flex items-center">
              <Monitor className="w-5 h-5 text-blue-400 mr-2" />
              System Event Stream
            </h2>
            <div className="space-y-3 font-mono text-sm overflow-y-auto max-h-[300px]">
              <AnimatePresence initial={false}>
                {logs.map((log) => (
                  <motion.div 
                    key={log.id}
                    initial={{ opacity: 0, x: -20 }}
                    animate={{ opacity: 1, x: 0 }}
                    className={`flex items-start ${log.type === 'error' ? 'text-red-400' : 'text-gray-300'}`}
                  >
                    <span className="text-gray-500 mr-4">[{log.time}]</span>
                    <span>{log.message}</span>
                  </motion.div>
                ))}
              </AnimatePresence>
              {logs.length === 0 && <p className="text-gray-600">Waiting for events...</p>}
            </div>
          </div>
        </div>
      </div>
      
      {/* Footer / System Architecture Map Preview */}
      <footer className="mt-10 pt-10 border-t border-white/5 flex justify-between text-gray-500 text-xs">
        <div className="flex gap-8">
          <div className="flex items-center"><ShieldCheck className="w-4 h-4 mr-2" /> Idempotent Commits</div>
          <div className="flex items-center"><Database className="w-4 h-4 mr-2" /> Event Sourced State</div>
          <div className="flex items-center"><Layers className="w-4 h-4 mr-2" /> Multi-Kitchen Mesh</div>
        </div>
        <div>DFS-CORE v1.0.4 - Distributed Food System DFS Reference Arch</div>
      </footer>
    </div>
  );
};

const SimulationButton = ({ label, subtext, variant, onClick }) => {
  const variants = {
    danger: 'hover:bg-red-500/10 border-red-500/20 text-red-100',
    warning: 'hover:bg-yellow-500/10 border-yellow-500/20 text-yellow-100',
    info: 'hover:bg-blue-500/10 border-blue-500/20 text-blue-100'
  };

  return (
    <button 
      onClick={onClick}
      className={`text-left p-4 rounded-xl border transition-all duration-300 ${variants[variant]}`}
    >
      <div className="font-bold">{label}</div>
      <div className="text-xs opacity-60 mt-1">{subtext}</div>
    </button>
  );
};

export default App;

