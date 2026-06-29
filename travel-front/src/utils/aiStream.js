/**
 * AI 流式对话（SSE）
 */
export function aiChatStream(params, onMessage, onError, signal) {
    const token = window.localStorage.getItem('user_token') || ''

    return fetch('/api/ai/chat/stream', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'x_access_token': token,
            'Cache-Control': 'no-cache',
            'Accept': 'text/event-stream'
        },
        body: JSON.stringify({
            ...params,
            _t: Date.parse(new Date()) / 1000
        }),
        signal
    }).then(async (response) => {
        if (response.status === 1011) {
            const err = new Error('登录已过期，请重新登录')
            err.code = 1011
            throw err
        }
        if (!response.ok) {
            throw new Error('AI 服务请求失败')
        }
        if (!response.body) {
            throw new Error('浏览器不支持流式响应')
        }

        const reader = response.body.getReader()
        const decoder = new TextDecoder('utf-8')
        let buffer = ''

        while (true) {
            const { done, value } = await reader.read()
            if (done) {
                break
            }
            buffer += decoder.decode(value, { stream: true })
            buffer = parseSseBuffer(buffer, onMessage, onError)
        }
    })
}

function parseSseBuffer(buffer, onMessage, onError) {
    const blocks = buffer.split('\n\n')
    const rest = blocks.pop() || ''

    blocks.forEach((block) => {
        if (!block.trim()) {
            return
        }

        let eventName = 'message'
        const dataLines = []

        block.split('\n').forEach((line) => {
            if (line.indexOf('event:') === 0) {
                eventName = line.slice(6).trim()
            } else if (line.indexOf('data:') === 0) {
                dataLines.push(line.slice(5).trim())
            }
        })

        if (!dataLines.length) {
            return
        }

        const rawData = dataLines.join('\n')
        try {
            const payload = JSON.parse(rawData)
            if (eventName === 'error') {
                onError && onError(new Error(typeof payload === 'string' ? payload : payload.message || 'AI 回复失败'))
                return
            }
            if (eventName === 'points') {
                onMessage && onMessage({ event: 'points', ...payload })
                return
            }
            onMessage && onMessage(payload)
        } catch (e) {
            console.error('解析 SSE 数据失败:', rawData, e)
        }
    })

    return rest
}
