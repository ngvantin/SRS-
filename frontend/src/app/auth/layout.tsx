import type React from "react"
import type { Metadata } from "next"

export const metadata: Metadata = {
    title: "Authentication",
    description: "Login, register, and manage your account",
}

export default function AuthLayout({
                                       children,
                                   }: {
    children: React.ReactNode
}) {
    return <div className="min-h-screen bg-gray-50">{children}</div>
}

