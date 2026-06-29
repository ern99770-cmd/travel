/**
 * 通过浏览器定位 + 逆地理编码获取当前城市/省份名称
 */
function extractLocationName(data) {
  if (!data) return ''
  const city = data.city || data.locality || ''
  if (city) {
    return city.replace(/(市|区|县)$/, '')
  }
  const province = data.principalSubdivision || ''
  if (province) {
    return province.replace(/(省|自治区|特别行政区|市)$/, '')
  }
  return ''
}

export function detectBrowserLocation() {
  return new Promise((resolve, reject) => {
    if (!navigator.geolocation) {
      reject(new Error('当前浏览器不支持定位'))
      return
    }
    navigator.geolocation.getCurrentPosition(
      (position) => {
        const { latitude, longitude } = position.coords
        fetch(
          `https://api.bigdatacloud.net/data/reverse-geocode-client?latitude=${latitude}&longitude=${longitude}&localityLanguage=zh`
        )
          .then(res => res.json())
          .then(data => {
            const name = extractLocationName(data)
            if (name) {
              resolve(name)
            } else {
              reject(new Error('无法解析当前位置'))
            }
          })
          .catch(() => reject(new Error('位置解析失败')))
      },
      (err) => {
        if (err.code === 1) {
          reject(new Error('定位权限被拒绝，请在浏览器中允许定位'))
        } else if (err.code === 2) {
          reject(new Error('无法获取位置信息'))
        } else if (err.code === 3) {
          reject(new Error('定位超时，请稍后重试'))
        } else {
          reject(new Error('定位失败'))
        }
      },
      { timeout: 12000, maximumAge: 60000, enableHighAccuracy: false }
    )
  })
}
