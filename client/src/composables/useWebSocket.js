import { ref, onMounted, onUnmounted } from 'vue'

export function useWebSocket(storeId) {
  const connected = ref(false)
  let ws = null
  let reconnectTimer = null
  const handlers = new Map()

  function connect(type = 'bar') {
    const protocol = location.protocol === 'https:' ? 'wss:' : 'ws:'
    const host = location.host
    ws = new WebSocket(`${protocol}//${host}/ws/${type}/${storeId}`)

    ws.onopen = () => {
      connected.value = true
    }

    ws.onmessage = (event) => {
      try {
        const msg = JSON.parse(event.data)
        const handler = handlers.get(msg.type)
        if (handler) handler(msg.data)
      } catch (e) {
        console.error('WebSocket message parse error', e)
      }
    }

    ws.onclose = () => {
      connected.value = false
      reconnectTimer = setTimeout(() => connect(type), 3000)
    }

    ws.onerror = () => {
      ws.close()
    }
  }

  function on(type, handler) {
    handlers.set(type, handler)
  }

  function disconnect() {
    if (reconnectTimer) clearTimeout(reconnectTimer)
    if (ws) ws.close()
    handlers.clear()
  }

  return { connected, connect, disconnect, on }
}
