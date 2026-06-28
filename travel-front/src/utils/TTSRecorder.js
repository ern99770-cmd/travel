import CryptoJS from "crypto-js"
const APPID = '3864d35b' // 从控制台可以获取
const API_SECRET = 'ZDI1NjRlNDMyYzk4MTg3NzZlZTFlMTYw' // 从控制台可以获取
const API_KEY = '731b9435630e1972458285c2a28076c7' // 从控制台可以获取
let total_res = "";

function getWebsocketUrl() {
  return new Promise((resolve, reject) => {
    try {
      var apiKey = API_KEY
      var apiSecret = API_SECRET
      var url = 'wss://spark-api.xf-yun.com/v4.0/chat'
      var host = 'spark-api.xf-yun.com'
      var date = new Date().toUTCString()
      var algorithm = 'hmac-sha256'
      var headers = 'host date request-line'
      var signatureOrigin = `host: ${host}\ndate: ${date}\nGET /v4.0/chat HTTP/1.1`
      var signatureSha = CryptoJS.HmacSHA256(signatureOrigin, apiSecret)
      var signature = CryptoJS.enc.Base64.stringify(signatureSha)
      var authorizationOrigin = `api_key="${apiKey}", algorithm="${algorithm}", headers="${headers}", signature="${signature}"`
      
      // 使用 CryptoJS 替代 btoa 确保兼容性
      var authorization = CryptoJS.enc.Base64.stringify(CryptoJS.enc.Utf8.parse(authorizationOrigin))
      
      url = `${url}?authorization=${authorization}&date=${encodeURIComponent(date)}&host=${host}`
      console.log('Generated WebSocket URL:', url)
      resolve(url)
    } catch (error) {
      console.error('Error generating WebSocket URL:', error)
      reject(error)
    }
  })
}


export default class TTSRecorder {
  constructor({appId = APPID} = {}) {
    this.appId = appId
    this.msgStore = null
    this.msgDom = null
  }

  // 连接websocket
  connectWebSocket() {
    return getWebsocketUrl().then(url => {
      let ttsWS
      if ('WebSocket' in window) {
        ttsWS = new WebSocket(url)
      } else if ('MozWebSocket' in window) {
        ttsWS = new MozWebSocket(url)
      } else {
        alert('浏览器不支持WebSocket')
        return
      }
      this.ttsWS = ttsWS
      ttsWS.onopen = e => {
        this.webSocketSend()
      }
      ttsWS.onmessage = e => {
        this.result(e.data)
      }
      ttsWS.onerror = e => {
        console.error('WebSocket Error Detail:', e)
        alert('AI 连接发生错误，请检查网络或 API 配置')
      }
      ttsWS.onclose = e => {
        console.log('WebSocket Closed:', e.code, e.reason)
        if (e.code !== 1000 && e.code !== 1005) {
          alert(`AI 连接意外关闭 (代码: ${e.code})，请刷新重试`)
        }
      }
    })
  }


  // websocket发送数据
  webSocketSend() {
    // 构建消息历史
    const list = this.msgStore.state.msg.list || []
    const messages = list.map(msg => ({
        role: msg.role,
        content: msg.content
    }))

    // 为第一条用户消息添加上下文引导，而不使用 system 角色
    if (messages.length > 0 && messages[0].role === 'user') {
        messages[0].content = `你是一个专业的财经顾问。请为用户提供详细、专业且贴心的建议。用户的问题是：${messages[0].content}`;
    }

    var params = {
        "header": {
            "app_id": this.appId,
        },
        "parameter": {
            "chat": {
                "domain": "4.0Ultra", // 使用 V4.0 Ultra 对应的域名
                "temperature": 0.7,
                "max_tokens": 4096
            }
        },
        "payload": {
            "message": {
                "text": messages
            }
        }
    }
    
    console.log('发送的参数：', params)
    this.ttsWS.send(JSON.stringify(params))
  }

  start(store, msgDom) {
    this.msgStore = store
    this.msgDom = msgDom
    
    // 如果已经有连接，先关闭
    if (this.ttsWS && (this.ttsWS.readyState === WebSocket.OPEN || this.ttsWS.readyState === WebSocket.CONNECTING)) {
        this.ttsWS.close()
    }
    
    this.connectWebSocket()
  }

  // websocket接收数据的处理
  result(resultData) {
    let jsonData;
    try {
        jsonData = JSON.parse(resultData);
    } catch (e) {
        console.error('JSON Parse Error:', e);
        return;
    }

    console.log('接收到的数据：', jsonData)
    
    // 检查错误码
    if (jsonData.header.code !== 0) {
        let errorMsg = `AI 提问失败: ${jsonData.header.code} - ${jsonData.header.message}`;
        if (jsonData.header.code === 11200) {
            errorMsg += '\n\n【权限解决建议】：\n1. 请检查代码开头的 APPID/Key/Secret 是否与讯飞控制台一致。\n2. 您在控制台开通的是否是 "Spark 4.0 Ultra"？如果开通的是 Lite/Pro/Max，请联系开发者修改代码中的 domain。';
        }
        alert(errorMsg);
        console.error(errorMsg);
        this.ttsWS.close();
        return;
    }

    if (!jsonData.payload || !jsonData.payload.choices) {
        console.log('等待数据响应...')
        return
    }

    const choices = jsonData.payload.choices;
    const text = choices.text;
    
    if (text && Array.isArray(text) && text.length > 0) {
        const content = text[0].content;
        if (content) {
            this.msgStore.dispatch('msg/aiAddMsg', {
                content: content,
                status: jsonData.header.status
            })
        }
    }

    // 检查会话是否完成
    if (jsonData.header.status === 2) {
        console.log('会话完成')
        this.ttsWS.close()
    }

    // 滚动到底部
    if (this.msgDom) {
        this.msgDom.scrollTop = this.msgDom.scrollHeight + 100
    }
  }
}
