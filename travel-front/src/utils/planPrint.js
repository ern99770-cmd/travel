/**
 * 打开行程打印预览
 */
export function printTravelPlan(plan, attractions = [], hotels = []) {
  const win = window.open('', '_blank')
  if (!win) {
    return false
  }

  const escapeHtml = (text) => {
    if (!text) return ''
    return String(text)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/\n/g, '<br/>')
  }

  const attractionHtml = attractions.length
    ? `<ul>${attractions.map(item => `<li>${escapeHtml(item.name)}${item.price != null ? ` · ¥${item.price}` : ''}</li>`).join('')}</ul>`
    : '<p>暂无推荐景点</p>'

  const hotelHtml = hotels.length
    ? `<ul>${hotels.map(item => `<li>${escapeHtml(item.name)}${item.address ? ` · ${escapeHtml(item.address)}` : ''}</li>`).join('')}</ul>`
    : '<p>暂无推荐酒店</p>'

  win.document.write(`
    <!DOCTYPE html>
    <html><head>
      <meta charset="utf-8"/>
      <title>${escapeHtml(plan.title || 'AI行程')}</title>
      <style>
        body { font-family: "Microsoft YaHei", sans-serif; padding: 32px; color: #333; line-height: 1.8; }
        h1 { font-size: 22px; margin-bottom: 8px; }
        .meta { color: #666; margin-bottom: 24px; font-size: 14px; }
        h2 { font-size: 16px; margin: 24px 0 12px; border-left: 4px solid #409EFF; padding-left: 8px; }
        ul { padding-left: 20px; }
        .content { white-space: pre-wrap; }
      </style>
    </head><body>
      <h1>${escapeHtml(plan.title || plan.destination + '行程')}</h1>
      <div class="meta">
        目的地：${escapeHtml(plan.destination)} |
        ${plan.days || '-'}天 |
        预算：${escapeHtml(plan.budget || '未设置')} |
        出发：${escapeHtml(plan.departureDate || '未设置')}
      </div>
      <h2>推荐景点</h2>
      ${attractionHtml}
      <h2>推荐酒店</h2>
      ${hotelHtml}
      <h2>详细行程</h2>
      <div class="content">${escapeHtml(plan.planContent || '')}</div>
    </body></html>
  `)
  win.document.close()
  win.focus()
  win.print()
  return true
}
